package alchemystar.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import alchemystar.engine.net.response.SelectResponse;
import alchemystar.expression.Expression;
import alchemystar.parser.Parser;
import alchemystar.parser.Prepared;
import alchemystar.parser.ddl.CreateTable;
import alchemystar.parser.dml.Select;
import alchemystar.result.ResultInterface;
import alchemystar.schema.Schema;
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
        if (tableMap == null) {
            throw new RuntimeException(
                    "You must specify the table path:" + tableName + ",use [set table_path=\"tableName:tablePath\"");
        }
        String tablePath = schemaMap.get(schemaName).get(tableName.toUpperCase());
        if (tablePath == null) {
            throw new RuntimeException("You must specify the table path,use [set table_path=\"tableName:tablePath\"");
        }
        return tablePath;
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
