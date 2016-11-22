package alchemystar.value;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import alchemystar.util.MathUtils;

/**
 * Implementation of the BYTE data type.
 *
 * @Author lizhuyang
 */
public class ValueByte extends Value {

    private final byte value;

    private ValueByte(byte value) {
        this.value = value;
    }

    public Value add(Value v) {
        ValueByte other = (ValueByte) v;
        return checkRange(value + other.value);
    }

    private static ValueByte checkRange(int x) {
        if (x < Byte.MIN_VALUE || x > Byte.MAX_VALUE) {
            throw new RuntimeException("Add Byte OverFlow,x=" + x);
        }
        return ValueByte.get((byte) x);
    }

    public int getSignum() {
        return Integer.signum(value);
    }

    public Value negate() {
        return checkRange(-(int) value);
    }

    public Value subtract(Value v) {
        ValueByte other = (ValueByte) v;
        return checkRange(value - other.value);
    }

    public Value multiply(Value v) {
        ValueByte other = (ValueByte) v;
        return checkRange(value * other.value);
    }

    public Value divide(Value v) {
        ValueByte other = (ValueByte) v;
        if (other.value == 0) {
            throw new RuntimeException("Can't Divide By Zero");
        }
        return ValueByte.get((byte) (value / other.value));
    }

    public Value modulus(Value v) {
        ValueByte other = (ValueByte) v;
        if (other.value == 0) {
            throw new RuntimeException("Can't Module By Zero");
        }
        return ValueByte.get((byte) (value % other.value));
    }

    public String getSQL() {
        return getString();
    }

    public int getType() {
        return Value.BYTE;
    }

    public byte getByte() {
        return value;
    }

    protected int compareSecure(Value o) {
        ValueByte v = (ValueByte) o;
        return MathUtils.compareInt(value, v.value);
    }

    public String getString() {
        return String.valueOf(value);
    }

    public int hashCode() {
        return value;
    }

    public Object getObject() {
        return Byte.valueOf(value);
    }

    public void set(PreparedStatement prep, int parameterIndex) throws SQLException {
        prep.setByte(parameterIndex, value);
    }

    /**
     * Get or create byte value for the given byte.
     *
     * @param i the byte
     *
     * @return the value
     */
    public static ValueByte get(byte i) {
        return (ValueByte) Value.cache(new ValueByte(i));
    }

    public boolean equals(Object other) {
        return other instanceof ValueByte && value == ((ValueByte) other).value;
    }

}
