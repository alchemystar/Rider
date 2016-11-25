package alchemystar.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import alchemystar.engine.net.proto.util.StringUtil;
import alchemystar.engine.net.response.SelectResponse;
import alchemystar.expression.Expression;
import alchemystar.parser.Parser;
import alchemystar.parser.Prepared;
import alchemystar.parser.ddl.CreateTable;
import alchemystar.parser.dml.Select;
import alchemystar.result.ResultInterface;
import alchemystar.schema.Schema;
import alchemystar.table.Table;
import alchemystar.util.PathUtil;
import alchemystar.value.Value;

/**
 * @Author lizhuyang
 */
public class Session {

    private final Database database;
    private String user;
    private Schema currentSchema;
    private String currentSchemaName;
    private int id;
    private Map<String, Map<String, String>> schemaMap;

    public Session(Database database, String user, int id) {
        this.database = database;
        this.user = user;
        this.id = id;
        schemaMap = new HashMap<String, Map<String, String>>();
    }

    // one session one thread,no concurrency
    public void loadTable(String tableName, String filePath) {
        Map<String, String> tableMap = schemaMap.get(currentSchemaName);
        if (tableMap == null) {
            tableMap = new HashMap<String, String>();
            schemaMap.put(currentSchemaName, tableMap);
        }
        tableMap.put(tableName.toUpperCase(), filePath);
    }

    public void loadTable(String schemaName, String tableName, String filePath) {
        Map<String, String> tableMap = schemaMap.get(schemaName);
        if (tableMap == null) {
            tableMap = new HashMap<String, String>();
            schemaMap.put(schemaName, tableMap);
        }
        tableMap.put(tableName.toUpperCase(), filePath);
    }

    public SelectResponse doQuery(String sql) {
        Parser parser = new Parser(this);
        Prepared prepared = parser.parse(sql);
        if (!(prepared instanceof Select)) {
            throw new RuntimeException("Only Support CreateTable And Select");
        }
        Select select = (Select) prepared;
        ArrayList<Expression> expressions = select.getExpressions();

        ResultInterface resultInterface = prepared.query();

        SelectResponse response = new SelectResponse(expressions.size());
        int columnCount = expressions.size();
        for (int i = 0; i < columnCount; i++) {
            Expression temp = expressions.get(i);
            response.addField(temp.getSQL(), temp.getType());
        }
        while (resultInterface.next()) {
            Value[] values = resultInterface.currentRow();
            ArrayList<String> row = new ArrayList<String>();
            for (int i = 0; i < columnCount; i++) {
                row.add(values[i].getString());
            }
            response.getRows().add(row);
        }
        response.setOriginCharset(select.getCharset());
        return response;
    }

    public void execute(String sql) {
        Parser parser = new Parser(this);
        Prepared prepared = parser.parse(sql);
        if (!(prepared instanceof CreateTable)) {
            throw new RuntimeException("Only Support CreateTable And Select");
        }
        prepared.update();
    }

    public void unloadTable(String tableName) {
        schemaMap.get(currentSchemaName).remove(tableName.toUpperCase());
    }

    public String getTablePath(String schemaName, String tableName) {
        Map<String, String> tableMap = schemaMap.get(schemaName);
        if (tableMap != null) {
            String tablePath = schemaMap.get(schemaName).get(tableName.toUpperCase());
            if (tablePath != null) {
                return tablePath;
            }
        }
        Schema schema = database.findSchema(schemaName);
        if (schema == null) {
            throw new RuntimeException("No such schema:" + schemaName);
        }
        Table table = schema.getTableOrView(tableName);
        // 如果本身session没有指定path,则用默认配置渲染的path
        if (StringUtil.isEmpty(table.getPathPattern())) {
            throw new RuntimeException(
                    "You must specify the table path:" + tableName + ",use [set table_path=\"tableName:tablePath\" or"
                            + " set pathPattern in the database.xml");

        }
        return PathUtil.renderPath(table.getPathPattern());
    }

    public Database getDatabase() {
        return database;
    }

    public Schema getCurrentSchema() {
        return currentSchema;
    }

    public void setCurrentSchema(Schema currentSchema) {
        this.currentSchema = currentSchema;
        this.currentSchemaName = currentSchema.getName();
    }

    public void changeCurrentSchema(String currentSchemaName) {
        Schema schema = database.findSchema(currentSchemaName);
        if (schema == null) {
            throw new RuntimeException("Unknown database " + currentSchemaName);
        }
        this.currentSchema = schema;
        this.currentSchemaName = currentSchemaName;
    }

    public String getCurrentSchemaName() {
        return currentSchemaName;
    }
}
