package alchemystar.value;

/**
 * @Author lizhuyang
 */
public class ValueBoolean extends Value {

    private static final Object TRUE = new ValueBoolean(true);
    private static final Object FALSE = new ValueBoolean(false);

    private Boolean value;

    private ValueBoolean(boolean value) {
        this.value = Boolean.valueOf(value);
    }

    @Override
    public int getType() {
        return Value.BOOLEAN;
    }

    public static ValueBoolean get(boolean b) {
        return (ValueBoolean) (b ? TRUE : FALSE);
    }

    @Override
    protected int compareSecure(Value o) {
        boolean v2 = ((ValueBoolean) o).value.booleanValue();
        boolean v = value.booleanValue();
        return (v == v2) ? 0 : (v ? 1 : -1);
    }

    public String getSQL() {
        return getString();
    }

    @Override
    public String getString() {
        if (value) {
            return "true";
        } else {
            return "false";
        }
    }

    public Boolean getBoolean() {
        return value;
    }

    public Value negate() {
        return (ValueBoolean) (value.booleanValue() ? FALSE : TRUE);
    }
}
