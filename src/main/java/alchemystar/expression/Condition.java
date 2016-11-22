package alchemystar.expression;

import alchemystar.value.Value;

/**
 * @Author lizhuyang
 */
public abstract class Condition extends Expression {
    @Override
    public int getType() {
        return Value.BOOLEAN;
    }
}
