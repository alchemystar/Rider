package alchemystar.value;

/**
 * @Author lizhuyang
 */
public class ValueNull extends Value {

    /**
     * The main NULL instance.
     */
    public static final ValueNull INSTANCE = new ValueNull();

    private ValueNull() {
        // don't allow construction
    }

    @Override
    public int getType() {
        return NULL;
    }

    @Override
    public String getString() {
        return getSQL();
    }

    public String getSQL() {
        return "NULL";
    }

    @Override
    protected int compareSecure(Value v) {
        throw new RuntimeException("ValueNull Not Support Compare");
    }
}
