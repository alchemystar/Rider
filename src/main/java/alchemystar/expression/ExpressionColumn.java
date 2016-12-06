package alchemystar.expression;

import alchemystar.engine.Session;
import alchemystar.table.Column;
import alchemystar.table.ColumnResolver;
import alchemystar.table.Table;
import alchemystar.table.TableFilter;
import alchemystar.util.CompareUtil;
import alchemystar.util.RiderStringUtils;
import alchemystar.value.Value;

/**
 * A expression that represents a column of a table or view.
 *
 * @Author lizhuyang
 */
public class ExpressionColumn extends Expression {

    private String columnName;
    private String tableAlias;
    private Column column;
    private ColumnResolver columnResolver;
    private int queryLevel;

    public ExpressionColumn(String tableAlias, String columnName) {
        this.tableAlias = tableAlias;
        this.columnName = columnName;
    }

    public ExpressionColumn(Column column) {
        this.column = column;
        this.columnName = column.getName();
    }

    @Override
    public int getType() {
        return column.getType();
    }

    @Override
    public Value getValue(Session session) {
        if (columnResolver == null) {
            String message = "can't map " + tableAlias + "." + columnName;
            throw new RuntimeException(message);
        }
        return columnResolver.getValue(column);
    }

    public String getTableName() {
        Table table = column.getTable();
        return table == null ? null : table.getName();
    }

    public String getColumnName() {
        return column.getName();
    }

    public TableFilter getTableFilter() {
        return columnResolver == null ? null : columnResolver.getTableFilter();
    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {
        // 如果tableAlias不相等 退出 等待下一个
        if (tableAlias != null && !tableAlias.equals(resolver.getTableAlias())) {
            return;
        }
        for (Column col : resolver.getColumns()) {
            String n = col.getName();
            if (CompareUtil.equalsIdentifiers(columnName, n)) {
                // 找到和当前columnName想对应的Resolver中的column
                mapColumn(resolver, col, level);
                return;
            }
        }
    }

    private void mapColumn(ColumnResolver resolver, Column col, int level) {
        if (this.columnResolver == null) {
            queryLevel = level;
            // 初始化column信息
            column = col;
            // 初始化resolver
            this.columnResolver = resolver;
        } else if (queryLevel == level && this.columnResolver != columnResolver) {
            throw new RuntimeException("ExpressionColumn map Column Exception");
        }
    }

    public String getSQL() {
        String sql;
        if (column != null) {
            sql = column.getSQL();
        } else {
            sql = RiderStringUtils.quoteIdentifier(columnName);
        }
        if (tableAlias != null) {
            sql = tableAlias + "." + sql;

        }
        return sql;
    }
}
