package alchemystar.expression;

import java.util.ArrayList;

import alchemystar.engine.Session;
import alchemystar.table.ColumnResolver;
import alchemystar.util.StatementBuilder;
import alchemystar.value.Value;
import alchemystar.value.ValueBoolean;
import alchemystar.value.ValueNull;

/**
 * @Author lizhuyang
 */
public class ConditionIn extends Condition {
    private Expression left;
    private final ArrayList<Expression> valueList;
    private int queryLevel;

    public ConditionIn(Expression left, ArrayList<Expression> valueList) {
        this.left = left;
        this.valueList = valueList;
    }

    public Value getValue(Session session) {
        Value l = left.getValue(session);
        if (l == ValueNull.INSTANCE) {
            return l;
        }
        boolean result = false;
        boolean hasNull = false;
        for (Expression e : valueList) {
            Value r = e.getValue(session);
            if (r == ValueNull.INSTANCE) {
                hasNull = true;
            } else {
                r = r.convertTo(l.getType());
                result = Comparison.compareNotNull(l, r, Comparison.EQUAL);
                if (result) {
                    break;
                }
            }
        }
        if (!result && hasNull) {
            return ValueNull.INSTANCE;
        }
        return ValueBoolean.get(result);
    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {
        left.mapColumns(resolver, level);
        for (Expression e : valueList) {
            e.mapColumns(resolver, level);
        }
        this.queryLevel = Math.max(level, this.queryLevel);
    }

    public String getSQL() {
        StatementBuilder buff = new StatementBuilder("(");
        buff.append(left.getSQL()).append(" IN(");
        for (Expression e : valueList) {
            buff.appendExceptFirst(", ");
            buff.append(e.getSQL());
        }
        return buff.append("))").toString();
    }
}
