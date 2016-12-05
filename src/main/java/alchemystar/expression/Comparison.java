package alchemystar.expression;

import alchemystar.engine.Session;
import alchemystar.table.ColumnResolver;
import alchemystar.util.CompareUtil;
import alchemystar.value.Value;
import alchemystar.value.ValueBoolean;

/**
 * Example comparison expressions are ID=1, NAME=NAME, NAME IS NULL.
 *
 * @Author lizhuyang
 */
public class Comparison extends Condition {
    // a=b
    public static final int EQUAL = 0;
    // a>=b
    public static final int BIGGER_EQUAL = 1;
    // a>b
    public static final int BIGGER = 2;
    // a<=b
    public static final int SMALLER_EQUAL = 3;
    // a<b
    public static final int SMALLER = 4;
    // a!=b
    public static final int NOT_EQUAL = 5;
    private int compareType;
    private Session session;
    private Expression left;
    private Expression right;

    public Comparison(Session sesion, int compareType, Expression left, Expression right) {
        this.compareType = compareType;
        this.left = left;
        this.right = right;
    }

    @Override
    public Value getValue(Session session) {
        if (left == null || right == null) {
            throw new RuntimeException("left and right must exist");
        }
        Value l = left.getValue(session);
        Value r = right.getValue(session);
        if (l.getType() != r.getType()) {
            if (!((l.getType() == Value.INT && r.getType() == Value.LONG) || (l.getType() == Value.LONG && r.getType()
                    == Value.INT
            ))) {
                throw new RuntimeException("left and right type must consist");
            }

        }
        boolean result = compare(l, r, compareType);
        return ValueBoolean.get(result);
    }

    static boolean compare(Value l, Value r, int compareType) {
        boolean result = false;
        switch (compareType) {
            case EQUAL:
                result = CompareUtil.areEqual(l, r);
                break;
            case NOT_EQUAL:
                result = !CompareUtil.areEqual(l, r);
                break;
            case BIGGER:
                result = CompareUtil.compare(l, r) > 0;
                break;
            case BIGGER_EQUAL:
                result = CompareUtil.compare(l, r) >= 0;
                break;
            case SMALLER:
                result = CompareUtil.compare(l, r) < 0;
                break;
            case SMALLER_EQUAL:
                result = CompareUtil.compare(l, r) <= 0;
                break;
            default:
                throw new RuntimeException("Type can't compare,Type=" + compareType);
        }

        return result;
    }

    static boolean compareNotNull(Value l, Value r, int compareType) {
        boolean result;
        switch (compareType) {
            case EQUAL:
                result = CompareUtil.areEqual(l, r);
                break;
            case NOT_EQUAL:
                result = !CompareUtil.areEqual(l, r);
                break;
            case BIGGER_EQUAL:
                result = CompareUtil.compare(l, r) >= 0;
                break;
            case BIGGER:
                result = CompareUtil.compare(l, r) > 0;
                break;
            case SMALLER_EQUAL:
                result = CompareUtil.compare(l, r) <= 0;
                break;
            case SMALLER:
                result = CompareUtil.compare(l, r) < 0;
                break;
            default:
                throw new RuntimeException("Type can't compare,Type=" + compareType);
        }
        return result;
    }

    static String getCompareOperator(int compareType) {
        switch (compareType) {
            case EQUAL:
                return "=";
            case BIGGER_EQUAL:
                return ">=";
            case BIGGER:
                return ">";
            case SMALLER_EQUAL:
                return "<=";
            case SMALLER:
                return "<";
            case NOT_EQUAL:
                return "<>";
            default:
                throw new RuntimeException("Unknown compareType=" + compareType);
        }
    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {
        left.mapColumns(resolver, level);
        if (right != null) {
            right.mapColumns(resolver, level);
        }
    }

    public String getSQL() {
        String sql;
        sql = left.getSQL() + " " + getCompareOperator(compareType) + " " + right.getSQL();
        return "(" + sql + ")";
    }

}
