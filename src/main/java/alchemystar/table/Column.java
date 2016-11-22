package alchemystar.table;

import alchemystar.expression.Expression;
import alchemystar.util.RiderStringUtils;
import alchemystar.value.DataType;
import alchemystar.value.Value;

/**
 * Column 行信息
 *
 * @Author lizhuyang
 */
public class Column {
    // Column类型
    private final int type;
    // Column名称
    private String name;
    // Column Index in table
    private int columnId;
    /// Column注释
    private String comment;
    // OriginalSql
    private String originalSQL;

    private Table table;

    private Expression defaultExpression;

    private boolean nullable = true;

    public Column(int type, String name) {
        this.type = type;
        this.name = name;
    }

    // 设置其table 和 columnId
    public void setTable(Table table, int columnId) {
        this.table = table;
        this.columnId = columnId;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColumnId() {
        return columnId;
    }

    public void setColumnId(int columnId) {
        this.columnId = columnId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Table getTable() {
        return table;
    }

    public String getCreateSQL() {
        StringBuilder buff = new StringBuilder();
        if (name != null) {
            buff.append(name).append(' ');
        }
        if (originalSQL != null) {
            buff.append(originalSQL);
        } else {
            buff.append(DataType.getTypeName(type));
        }
        if (defaultExpression != null) {
            String sql = defaultExpression.getSQL();
            buff.append(" DEFAULT ").append(sql);
        }

        if (!nullable)

        {
            buff.append(" NOT NULL");
        }

        if (comment != null)

        {
            buff.append(" COMMENT ").append(RiderStringUtils.quoteStringSQL(comment));
        }

        return buff.toString();
    }

    public Value convert(Value v) {
        return v.convertTo(type);
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public void setOriginalSQL(String original) {
        originalSQL = original;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public void setDefaultExpression(Expression defaultExpression) {
        this.defaultExpression = defaultExpression;
    }

    public String getSQL() {
        return name;
    }
}
