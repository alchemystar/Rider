package alchemystar.value;

import java.math.BigInteger;

import alchemystar.util.MathUtils;

/**
 * @Author lizhuyang
 */
public class ValueLong extends Value {

    public static final BigInteger MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private static final BigInteger MIN = BigInteger.valueOf(Long.MIN_VALUE);

    public final long value;
    // 0~999直接缓存起来
    private static final int VALUE_DEFAULT_NUMBER = 1000;

    private static final ValueLong[] valuesCache;

    private ValueLong(long value) {
        this.value = value;
    }

    static {
        valuesCache = new ValueLong[VALUE_DEFAULT_NUMBER];
        for (int i = 0; i < VALUE_DEFAULT_NUMBER; i++) {
            valuesCache[i] = new ValueLong((long) i);
        }
    }

    @Override
    public int getType() {
        return Value.LONG;
    }

    @Override
    public String getString() {
        return String.valueOf(value);
    }

    public long getLong() {
        return value;
    }

    public static ValueLong get(Long value) {
        if (0 <= value && value < 1000) {
            return valuesCache[value.intValue()];
        }
        // todo cache
        return new ValueLong(value);
    }

    @Override
    protected int compareSecure(Value o) {
        ValueLong v = (ValueLong) o;
        return MathUtils.compareLong(value, v.value);
    }

    public Value negate() {
        if (value == Long.MIN_VALUE) {
            throw new RuntimeException("Over Flow Exception");
        }
        return ValueLong.get(-value);
    }

    public Value add(Value v) {
        if (v.getType() == Value.LONG) {
            ValueLong other = (ValueLong) v;
            long result = value + other.value;
            int sv = Long.signum(value);
            int so = Long.signum(other.value);
            int sr = Long.signum(result);
            // if the operands have different signs overflow can not occur
            // if the operands have the same sign,
            // and the result has a different sign, then it is an overflow
            // it can not be an overflow when one of the operands is 0
            if (sv != so || sr == so || sv == 0 || so == 0) {
                return ValueLong.get(result);
            }
            throw new RuntimeException("Over Flow Exception");
        }
        if (v.getType() == Value.INT) {
            ValueInt other = (ValueInt) v;
            long result = value + other.value;
            int sv = Long.signum(value);
            int so = Long.signum(other.value);
            int sr = Long.signum(result);
            // if the operands have different signs overflow can not occur
            // if the operands have the same sign,
            // and the result has a different sign, then it is an overflow
            // it can not be an overflow when one of the operands is 0
            if (sv != so || sr == so || sv == 0 || so == 0) {
                return ValueLong.get(result);
            }
            throw new RuntimeException("Over Flow Exception");
        }
        throw new RuntimeException("Can't convert from " + this.getType() + " to " + v.getType());
    }

    public Value subtract(Value v) {
        if (v.getType() == Value.LONG) {
            ValueLong other = (ValueLong) v;
            int sv = Long.signum(value);
            int so = Long.signum(other.value);
            // if the operands have the same sign, then overflow can not occur
            // if the second operand is 0, then overflow can not occur
            if (sv == so || so == 0) {
                return ValueLong.get(value - other.value);
            }
            // now, if the other value is Long.MIN_VALUE, it must be an overflow
            // x - Long.MIN_VALUE overflows for x>=0
            return add(other.negate());
        }
        if (v.getType() == Value.INT) {
            ValueInt other = (ValueInt) v;
            int sv = Long.signum(value);
            int so = Long.signum(other.value);
            // if the operands have the same sign, then overflow can not occur
            // if the second operand is 0, then overflow can not occur
            if (sv == so || so == 0) {
                return ValueLong.get(value - other.value);
            }
            // now, if the other value is Long.MIN_VALUE, it must be an overflow
            // x - Long.MIN_VALUE overflows for x>=0
            return add(other.negate());
        }
        throw new RuntimeException("Can't convert from " + this.getType() + " to " + v.getType());
    }

    public Value multiply(Value v) {
        if (v.getType() == Value.LONG) {
            ValueLong other = (ValueLong) v;
            long result = value * other.value;
            if (value == 0 || value == 1 || other.value == 0 || other.value == 1) {
                return ValueLong.get(result);
            }
            if (isInteger(value) && isInteger(other.value)) {
                return ValueLong.get(result);
            }
            BigInteger bv = BigInteger.valueOf(value);
            BigInteger bo = BigInteger.valueOf(other.value);
            BigInteger br = bv.multiply(bo);
            if (br.compareTo(MIN) < 0 || br.compareTo(MAX) > 0) {
                throw new RuntimeException("Over Flow Exception");
            }
            return ValueLong.get(br.longValue());
        }
        if (v.getType() == Value.INT) {
            ValueInt other = (ValueInt) v;
            long result = value * other.value;
            if (value == 0 || value == 1 || other.value == 0 || other.value == 1) {
                return ValueLong.get(result);
            }
            if (isInteger(value) && isInteger(other.value)) {
                return ValueLong.get(result);
            }
            BigInteger bv = BigInteger.valueOf(value);
            BigInteger bo = BigInteger.valueOf(other.value);
            BigInteger br = bv.multiply(bo);
            if (br.compareTo(MIN) < 0 || br.compareTo(MAX) > 0) {
                throw new RuntimeException("Over Flow Exception");
            }
            return ValueLong.get(br.longValue());
        }
        throw new RuntimeException("Can't convert from " + this.getType() + " to " + v.getType());
    }

    public Value divide(Value v) {
        if (v.getType() == Value.LONG) {
            ValueLong other = (ValueLong) v;
            if (other.value == 0) {
                throw new RuntimeException("Can't divide by 0");
            }
            return ValueLong.get(value / other.value);
        }
        if (v.getType() == Value.INT) {
            ValueInt other = (ValueInt) v;
            if (other.value == 0) {
                throw new RuntimeException("Can't divide by 0");
            }
            return ValueLong.get(value / other.value);
        }
        throw new RuntimeException("Can't convert from " + this.getType() + " to " + v.getType());
    }

    public Value modulus(Value v) {
        if (v.getType() == Value.LONG) {
            ValueLong other = (ValueLong) v;
            if (other.value == 0) {
                throw new RuntimeException("Can't module by 0");
            }
            return ValueLong.get(this.value % other.value);
        }
        if (v.getType() == Value.INT) {
            ValueInt other = (ValueInt) v;
            if (other.value == 0) {
                throw new RuntimeException("Can't module by 0");
            }
            return ValueLong.get(this.value % other.value);
        }
        throw new RuntimeException("Can't convert from " + this.getType() + " to " + v.getType());
    }

    public String getSQL() {
        return getString();
    }

    public int getSignum() {
        return Long.signum(value);
    }

    private static boolean isInteger(long a) {
        return a >= Integer.MIN_VALUE && a <= Integer.MAX_VALUE;
    }
}
