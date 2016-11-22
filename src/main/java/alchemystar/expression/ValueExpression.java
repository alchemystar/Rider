package alchemystar.expression;

import alchemystar.engine.Session;
import alchemystar.table.ColumnResolver;
import alchemystar.value.Value;
import alchemystar.value.ValueNull;

/**
 * An expression representing a constant value.¬
 *
 * @Author lizhuyang
 */
public class ValueExpression extends Expression {

    private static final Object NULL = new ValueExpression(ValueNull.INSTANCE);
    private static final Object DEFAULT = new ValueExpression(ValueNull.INSTANCE);
    private final Value value;

    private ValueExpression(Value value) {
        this.value = value;
    }

    public static ValueExpression getNull() {
        return (ValueExpression) NULL;
    }

    public static ValueExpression getDefault() {
        return (ValueExpression) DEFAULT;
    }

    public static ValueExpression get(Value value) {
        if (value == ValueNull.INSTANCE) {
            return getNull();
        }
        return new ValueExpression(value);
    }

    public Value getValue(Session session) {
        return value;
    }

    public int getType() {
        return value.getType();
    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {

    }

    public String getSQL() {
        if (this == DEFAULT) {
            return "DEFAULT";
        }
        return value.getSQL();
    }
}
