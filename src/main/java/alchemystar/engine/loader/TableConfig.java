package alchemystar.engine.loader;

/**
 * sql
 *
 * @Author lizhuyang
 */
public class TableConfig {

    private String sql;

    private int skipRows = 0;

    private String path;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public int getSkipRows() {
        return skipRows;
    }

    public void setSkipRows(int skipRows) {
        this.skipRows = skipRows;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
