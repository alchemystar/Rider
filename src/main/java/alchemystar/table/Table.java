package alchemystar.table;

import java.util.HashMap;

import alchemystar.engine.config.SystemConfig;
import alchemystar.parser.ddl.CreateTableData;
import alchemystar.schema.Schema;
import alchemystar.value.Value;

/**
 * Table 表信息
 *
 * @Author lizhuyang
 */
public class Table {

    protected Column[] columns;
    protected int id;
    protected HashMap<String, Column> columnMap;
    protected String name;
    protected Schema schema;
    protected boolean isHidden;
    protected String comment;
    protected String tableEngine;
    protected String seperator;
    protected boolean skipWrong;
    protected String originSql;
    protected int skipRows;
    protected String pathPattern;
    // if viewSql is not empty,则它是视图
    protected String viewSql;
    protected String charset;

    public Table(Schema schema, int i, String name, String seperator, boolean skipWrong) {
        this.name = name;
        this.schema = schema;
        this.id = i;
        this.seperator = seperator;
        this.skipWrong = skipWrong;
        columnMap = new HashMap<String, Column>();
        this.charset = SystemConfig.DEFAULT_CHARSET;
    }

    public Table(Schema schema, int i, CreateTableData data) {
        this.name = data.tableName;
        this.schema = schema;
        this.id = i;
        this.seperator = data.seperator;
        this.skipWrong = data.skipWrong;
        columnMap = new HashMap<String, Column>();
        this.charset = data.charset;
    }

    public Column[] getColumns() {
        return columns;
    }

    // init columns and the columnMap
    public void setColumns(Column[] columns) {
        this.columns = columns;
        if (columnMap.size() > 0) {
            columnMap.clear();
        }
        for (int i = 0; i < columns.length; i++) {
            Column col = columns[i];
            int dataType = col.getType();
            if (dataType == Value.UNKNOWN) {
                throw new RuntimeException("Value Type UnKnown");
            }
            col.setTable(this, i);
            String columnName = col.getName();
            if (columnMap.get(columnName) != null) {
                throw new RuntimeException("Duplicated Column Name:" + columnName);
            }
            columnMap.put(columnName, col);
        }
    }

    public String getSeperator() {
        return seperator;
    }

    public void setSeperator(String seperator) {
        this.seperator = seperator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        this.isHidden = hidden;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public String getTableEngine() {
        return tableEngine;
    }

    public void setTableEngine(String tableEngine) {
        this.tableEngine = tableEngine;
    }

    public boolean isSkipWrong() {
        return skipWrong;
    }

    public void setSkipWrong(boolean skipWrong) {
        this.skipWrong = skipWrong;
    }

    public String getOriginSql() {
        return originSql;
    }

    public void setOriginSql(String originSql) {
        this.originSql = originSql;
    }

    public int getSkipRows() {
        return skipRows;
    }

    public void setSkipRows(int skipRows) {
        this.skipRows = skipRows;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getViewSql() {
        return viewSql;
    }

    public void setViewSql(String viewSql) {
        this.viewSql = viewSql;
    }
}
