package alchemystar.value;

import alchemystar.util.StatementBuilder;

/**
 * Implementation of the ARRAY data type.
 *
 * @Author lizhuyang
 */
public class ValueArray extends Value {
    private final Class<?> componentType;
    private final Value[] values;
    private int hash;

    private ValueArray(Class<?> componentType, Value[] list) {
        this.componentType = componentType;
        this.values = list;
    }

    private ValueArray(Value[] list) {
        this(Object.class, list);
    }

    public static ValueArray get(Value[] list) {
        return new ValueArray(list);
    }

    public static ValueArray get(Class<?> componentType, Value[] list) {
        return new ValueArray(componentType, list);
    }

    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        int h = 1;
        for (Value v : values) {
            h = h * 31 + v.hashCode();
        }
        hash = h;
        return h;
    }

    protected int compareSecure(Value o) {
        ValueArray v = (ValueArray) o;
        if (values == v.values) {
            return 0;
        }
        int l = values.length;
        int ol = v.values.length;
        int len = Math.min(l, ol);
        for (int i = 0; i < len; i++) {
            Value v1 = values[i];
            Value v2 = v.values[i];
            int comp = v1.compareTo(v2);
            if (comp != 0) {
                return comp;
            }
        }
        return l > ol ? 1 : l == ol ? 0 : -1;
    }

    public boolean equals(Object other) {
        if (!(other instanceof ValueArray)) {
            return false;
        }
        ValueArray v = (ValueArray) other;
        if (values == v.values) {
            return true;
        }
        int len = values.length;
        if (len != v.values.length) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (!values[i].equals(v.values[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getType() {
        return Value.ARRAY;
    }

    @Override
    public String getString() {
        return getSQL();
    }

    public String getSQL() {
        StatementBuilder buff = new StatementBuilder("(");
        for (Value v : values) {
            buff.appendExceptFirst(", ");
            buff.append(v.getSQL());
        }
        if (values.length == 1) {
            buff.append(',');
        }
        return buff.append(')').toString();
    }
}
