package alchemystar.expression;

import alchemystar.engine.Session;
import alchemystar.table.Column;
import alchemystar.table.ColumnResolver;
import alchemystar.util.StatementBuilder;
import alchemystar.value.Value;
import alchemystar.value.ValueArray;

/**
 * A list of expressions, as in (ID, NAME).
 * The result of this expression is an array.
 */
public class ExpressionList extends Expression {

    private Expression[] list;

    public ExpressionList(Expression[] list) {
        this.list = list;
    }

    public Value getValue(Session session) {
        Value[] v = new Value[list.length];
        for (int i = 0; i < list.length; i++) {
            v[i] = list[i].getValue(session);
        }
        return ValueArray.get(v);
    }

    public int getType() {
        return Value.ARRAY;
    }

    public void mapColumns(ColumnResolver resolver, int level) {
        for (Expression e : list) {
            e.mapColumns(resolver, level);
        }
    }

    public Expression[] getExpressionColumns(Session session) {
        ExpressionColumn[] expr = new ExpressionColumn[list.length];
        for (int i = 0; i < list.length; i++) {
            Expression e = list[i];
            Column col = new Column(e.getType(), "C" + (i + 1));
            expr[i] = new ExpressionColumn(col);
        }
        return expr;
    }

    public String getSQL() {
        StatementBuilder buff = new StatementBuilder("(");
        for (Expression e : list) {
            buff.appendExceptFirst(", ");
            buff.append(e.getSQL());
        }
        if (list.length == 1) {
            buff.append(',');
        }
        return buff.append(')').toString();
    }

}
