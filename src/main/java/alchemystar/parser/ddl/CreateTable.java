package alchemystar.parser.ddl;

import java.util.ArrayList;

import alchemystar.engine.Database;
import alchemystar.engine.Session;
import alchemystar.engine.loader.TableConfig;
import alchemystar.expression.Expression;
import alchemystar.parser.Prepared;
import alchemystar.parser.Query;
import alchemystar.schema.Schema;
import alchemystar.table.Column;
import alchemystar.table.Table;

/**
 * CreateTable
 *
 * @Author lizhuyang
 */
public class CreateTable extends Prepared {

    private Schema schema;
    private final CreateTableData data = new CreateTableData();
    private Column[] pkColumns;
    private String comment;
    private Query asQuery;
    private boolean ifNotExists;
    private String originSql;

    public CreateTable(Session session, Schema schema) {
        super(session);
        this.schema = schema;
    }

    public void setSeperator(String seperator) {
        data.seperator = seperator;
    }

    public void setQuery(Query query) {
        this.asQuery = query;
    }

    public void addColumn(Column column) {
        data.columns.add(column);
    }

    private void generateColumnsFromQuery() {
        int columnCount = asQuery.getColumnCount();
        ArrayList<Expression> expressions = asQuery.getExpressions();
        for (int i = 0; i < columnCount; i++) {
            Expression expr = expressions.get(i);
            int type = expr.getType();
            String name = expr.getColumnName();
            Column col = new Column(type, name);
            addColumn(col);
        }

    }

    /**
     * 此方法支持联合主键
     *
     * @param columns
     *
     * @return
     */
    private boolean setPrimaryKeyColumns(Column[] columns) {
        if (pkColumns != null) {
            throw new RuntimeException("Duplicated Key PkColumns");
        }
        this.pkColumns = columns;
        return false;
    }

    @Override
    public int update() {
        Database db = session.getDatabase();
        if (schema.getTableOrView(data.tableName) != null) {
            if (ifNotExists) {
                return converTable(db);
            }
            throw new RuntimeException("Table already exists");
        }
        // No Support AsQuery Not
        // No pkColumns Now
        data.id = getObjectId();
        data.create = create;
        data.sesison = session;
        Table table = schema.createTable(data);
        table.setOriginSql(originSql);
        table.setComment(comment);
        db.addTable(table);
        return 0;
    }

    public int update(TableConfig tableConfig) {
        Database db = session.getDatabase();
        if (schema.getTableOrView(data.tableName) != null) {
            if (ifNotExists) {
                return converTable(db);
            }
            throw new RuntimeException("Table already exists");
        }
        // No Support AsQuery Not
        // No pkColumns Now
        data.id = getObjectId();
        data.create = create;
        data.sesison = session;
        Table table = schema.createTable(data);
        table.setOriginSql(originSql);
        table.setComment(comment);
        table.setSkipRows(tableConfig.getSkipRows());
        table.setPathPattern(tableConfig.getPath());
        db.addTable(table);
        return 0;
    }

    private int converTable(Database db) {
        data.id = getObjectId();
        data.create = create;
        data.sesison = session;
        Table table = schema.createTable(data);
        table.setComment(comment);
        db.coverTable(table);
        return 0;
    }

    public void setTableName(String tableName) {
        data.tableName = tableName;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setTableEngine(String tableEngine) {
        data.tableEngine = tableEngine;
    }

    public void setHidden(boolean isHidden) {
        data.isHidden = isHidden;
    }

    public void setWrongSkip(boolean skipWrong) {
        data.skipWrong = skipWrong;
    }

    public String getOriginSql() {
        return originSql;
    }

    public void setOriginSql(String originSql) {
        this.originSql = originSql;
    }

    public void setCharset(String charset) {
        data.charset = charset;
    }
}
