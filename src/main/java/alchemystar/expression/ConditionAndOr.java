package alchemystar.expression;

import alchemystar.engine.Session;
import alchemystar.table.ColumnResolver;
import alchemystar.table.TableFilter;
import alchemystar.value.Value;
import alchemystar.value.ValueBoolean;
import alchemystar.value.ValueNull;

/**
 * An 'and' or 'or' condition as in WHERE ID=1 AND NAME=?
 */
public class ConditionAndOr extends Condition {
    /**
     * The AND condition type as in ID=1 AND NAME='Hello'.
     */
    public static final int AND = 0;

    /**
     * The OR condition type as in ID=1 OR NAME='Hello'.
     */
    public static final int OR = 1;

    private final int andOrType;
    // 左右条件
    private Expression left, right;

    public ConditionAndOr(int andOrType, Expression left, Expression right) {
        this.andOrType = andOrType;
        this.left = left;
        this.right = right;
    }

    @Override
    public Value getValue(Session session) {
        Value l = left.getValue(session);
        Value r;
        switch (andOrType) {
            case AND: {
                if (Boolean.FALSE.equals(l.getBoolean())) {
                    return l;
                }
                r = right.getValue(session);
                if (Boolean.FALSE.equals(r.getBoolean())) {
                    return r;
                }
                if (l == ValueNull.INSTANCE) {
                    return l;
                }
                if (r == ValueNull.INSTANCE) {
                    return r;
                }
                return ValueBoolean.get(true);
            }
            case OR: {
                if (Boolean.TRUE.equals(l.getBoolean())) {
                    return l;
                }
                r = right.getValue(session);
                if (Boolean.TRUE.equals(r.getBoolean())) {
                    return r;
                }
                if (l == ValueNull.INSTANCE) {
                    return l;
                }
                if (r == ValueNull.INSTANCE) {
                    return r;
                }
                return ValueBoolean.get(false);
            }
            default:
                throw new RuntimeException("Can't convert to boolean in ConditionAndOr ");
        }

    }

    @Override
    public void addFilterConditions(TableFilter filter) {
        if (andOrType == AND) {
            left.addFilterConditions(filter);
            right.addFilterConditions(filter);
        } else {
            super.addFilterConditions(filter);
        }
    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {
        left.mapColumns(resolver, level);
        right.mapColumns(resolver, level);
    }

    public Expression getExpression(boolean getLeft) {
        return getLeft ? this.left : right;
    }

    public String getSQL() {
        String sql;
        switch (andOrType) {
            case AND:
                sql = left.getSQL() + "\n    AND " + right.getSQL();
                break;
            case OR:
                sql = left.getSQL() + "\n    OR " + right.getSQL();
                break;
            default:
                throw new RuntimeException("Unknown andOrType=" + andOrType);
        }
        return "(" + sql + ")";
    }

}
