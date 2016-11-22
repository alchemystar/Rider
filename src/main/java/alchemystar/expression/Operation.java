package alchemystar.expression;

import alchemystar.engine.Session;
import alchemystar.table.ColumnResolver;
import alchemystar.value.Value;
import alchemystar.value.ValueNull;
import alchemystar.value.ValueString;

/**
 * A mathematical expression, or string concatenation.
 *
 * @Author lizhuyang
 */
public class Operation extends Expression {
    /**
     * This operation represents a string concatenation as in 'Hello' || 'World'.
     */
    public static final int CONCAT = 0;

    /**
     * This operation represents an addition as in 1 + 2.
     */
    public static final int PLUS = 1;

    /**
     * This operation represents a subtraction as in 2 - 1.
     */
    public static final int MINUS = 2;

    /**
     * This operation represents a multiplication as in 2 * 3.
     */
    public static final int MULTIPLY = 3;

    /**
     * This operation represents a division as in 4 * 2.
     */
    public static final int DIVIDE = 4;

    /**
     * This operation represents a negation as in - ID.
     */
    public static final int NEGATE = 5;

    /**
     * This operation represents a modulus as in 5 % 2.
     */
    public static final int MODULUS = 6;

    private int opType;
    private Expression left, right;
    private int dataType;
    private boolean convertRight = true;

    public Operation(int opType, Expression left, Expression right) {
        this.opType = opType;
        this.left = left;
        this.right = right;
    }

    private String getOperationToken() {
        switch (opType) {
            case NEGATE:
                return "-";
            case CONCAT:
                return "||";
            case PLUS:
                return "+";
            case MINUS:
                return "-";
            case MULTIPLY:
                return "*";
            case DIVIDE:
                return "/";
            case MODULUS:
                return "%";
            default:
                throw new RuntimeException("UnKnown opType=" + opType);
        }
    }

    public Value getValue(Session session) {
        Value l = left.getValue(session).convertTo(dataType);
        Value r;
        if (right == null) {
            r = null;
        } else {
            r = right.getValue(session);
            if (convertRight) {
                r = r.convertTo(dataType);
            }
        }
        switch (opType) {
            case NEGATE:
                return l == ValueNull.INSTANCE ? l : l.negate();
            case CONCAT:
                if (l == ValueNull.INSTANCE) {
                    return r;
                } else if (r == ValueNull.INSTANCE) {
                    return l;
                }
                String s1 = l.getString();
                String s2 = r.getString();
                StringBuilder buff = new StringBuilder(s1.length() + s2.length());
                buff.append(s1).append(s2);
                return ValueString.get(buff.toString());
            case PLUS:
                if (l == ValueNull.INSTANCE || r == ValueNull.INSTANCE) {
                    return ValueNull.INSTANCE;
                }
                return l.add(r);
            case MINUS:
                if (l == ValueNull.INSTANCE || r == ValueNull.INSTANCE) {
                    return ValueNull.INSTANCE;
                }
                return l.subtract(r);
            case MULTIPLY:
                if (l == ValueNull.INSTANCE || r == ValueNull.INSTANCE) {
                    return ValueNull.INSTANCE;
                }
                return l.multiply(r);
            case DIVIDE:
                if (l == ValueNull.INSTANCE || r == ValueNull.INSTANCE) {
                    return ValueNull.INSTANCE;
                }
                return l.divide(r);
            case MODULUS:
                if (l == ValueNull.INSTANCE || r == ValueNull.INSTANCE) {
                    return ValueNull.INSTANCE;
                }
                return l.modulus(r);
            default:
                throw new RuntimeException("UnKnown optype:" + opType);
        }
    }

    public void mapColumns(ColumnResolver resolver, int level) {
        left.mapColumns(resolver, level);
        if (right != null) {
            right.mapColumns(resolver, level);
        }
    }

    public int getType() {
        return dataType;
    }

    public String getSQL() {
        String sql;
        if (opType == NEGATE) {
            // don't remove the space, otherwise it might end up some thing like
            // --1 which is a line remark
            sql = "- " + left.getSQL();
        } else {
            // don't remove the space, otherwise it might end up some thing like
            // --1 which is a line remark
            sql = left.getSQL() + " " + getOperationToken() + " " + right.getSQL();
        }
        return "(" + sql + ")";
    }
}
