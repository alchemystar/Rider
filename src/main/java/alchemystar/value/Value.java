package alchemystar.value;

import java.lang.ref.SoftReference;

import alchemystar.constant.SysProperties;

/**
 * 统一的Value转换体系
 *
 * @Author lizhuyang
 */
public abstract class Value {

    private static SoftReference<Value[]> softCache = new SoftReference<Value[]>(null);
    // Now Only Support String and Long
    public static final int UNKNOWN = -1;
    public static final int NULL = 0;
    public static final int LONG = 1;
    public static final int STRING = 2;
    public static final int BOOLEAN = 3;
    public static final int ARRAY = 4;
    public static final int INT = 4;
    public static final int BYTE = 5;

    public abstract int getType();

    public final int compareTo(Value v) {
        if (this == v) {
            return 0;
        }
        if (this.getType() != v.getType()) {
            throw new RuntimeException("Can't compare two different type!");
        }
        return this.compareSecure(v);
    }

    protected abstract int compareSecure(Value v);

    public Value convertTo(int targetType) {
        int sourceType = getType();
        if (sourceType == getType()) {
            return this;
        }
        try {
            switch (targetType) {
                case LONG:
                    switch (sourceType) {
                        case STRING:
                            ValueString value = (ValueString) this;
                            return ValueLong.get(Long.valueOf(value.getString()));
                        case BOOLEAN:
                            throw new RuntimeException("Can't convert to boolean");
                        case INT:
                            ValueInt valueInt = (ValueInt) this;
                            return ValueLong.get(valueInt.getLong());
                        case BYTE:
                            ValueByte valueByte = (ValueByte) this;
                            return ValueLong.get((long) valueByte.getByte());
                        default:
                            throw new RuntimeException("Unknown SourceType={}" + sourceType);
                    }
                case INT:
                    switch (sourceType) {
                        case LONG:
                            ValueLong valueLong = (ValueLong) this;
                            return ValueInt.get((int) valueLong.getLong());
                        case BOOLEAN:
                            throw new RuntimeException("Can't convert to boolean");
                        case STRING:
                            ValueString value = (ValueString) this;
                            return ValueInt.get(Integer.valueOf(value.getString()));
                        case BYTE:
                            ValueByte valueByte = (ValueByte) this;
                            return ValueInt.get((int) valueByte.getByte());
                        default:
                            throw new RuntimeException("Can't convert to boolean");
                    }
                case BYTE:
                    switch (sourceType) {
                        case STRING:
                            ValueString value = (ValueString) this;
                            return ValueByte.get(Byte.valueOf(value.getString()));
                        default:
                            throw new RuntimeException("Can't convert to boolean");
                    }
                case STRING:
                    switch (sourceType) {
                        case LONG:
                            ValueLong value = (ValueLong) this;
                            return ValueString.get(String.valueOf(value.getLong()));
                        case BOOLEAN:
                            ValueBoolean vb = (ValueBoolean) this;
                            if (vb.getBoolean()) {
                                return ValueString.get("true");
                            } else {
                                return ValueString.get("false");
                            }
                        case INT:
                            ValueInt valueInt = (ValueInt) this;
                            return ValueString.get(valueInt.getString());
                        case BYTE:
                            ValueByte valueByte = (ValueByte) this;
                            return ValueString.get(valueByte.getString());
                        default:
                            throw new RuntimeException("Unknown SourceType={}" + sourceType);
                    }
                case BOOLEAN:
                    switch (sourceType) {
                        case LONG:
                            ValueLong valueLong = (ValueLong) this;
                            return ValueBoolean.get(Long.signum(valueLong.getLong()) != 0);
                        case STRING:
                            String s = getString();
                            if (s.equalsIgnoreCase("true") ||
                                    s.equalsIgnoreCase("t") ||
                                    s.equalsIgnoreCase("yes") ||
                                    s.equalsIgnoreCase("y")) {
                                return ValueBoolean.get(true);
                            } else if (s.equalsIgnoreCase("false") ||
                                    s.equalsIgnoreCase("f") ||
                                    s.equalsIgnoreCase("no") ||
                                    s.equalsIgnoreCase("n")) {
                                return ValueBoolean.get(false);
                            } else {
                                Long num = Long.valueOf(s);
                                // num <> 0 表明是true,=0是false
                                return ValueBoolean.get(Long.signum(num) != 0);
                            }
                        case INT:
                            ValueInt valueInt = (ValueInt) this;
                            return ValueBoolean.get(Integer.signum(valueInt.getInt()) != 0);
                        case BYTE:
                            ValueByte valueByte = (ValueByte) this;
                            return ValueBoolean.get(Integer.signum(valueByte.getByte()) != 0);
                        default:
                            throw new RuntimeException("Can't convert to boolean");
                    }
                default:
                    throw new RuntimeException("Unknown TargetType={}" + targetType);

            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Convert To targetType error,sourceType=" + sourceType + "|targetType=" + targetType, e);
        }
    }

    // Get Value As String

    public abstract String getString();

    // Get Value As Long
    public long getLong() {
        return ((ValueLong) convertTo(LONG)).getLong();
    }

    public Boolean getBoolean() {
        return ((ValueBoolean) convertTo(BOOLEAN)).getBoolean();
    }

    public Value negate() {
        throw new RuntimeException("UnSupport Negate Function");
    }

    public Value add(Value v) {
        throw new RuntimeException("UnSupport Plus Function");
    }

    public Value subtract(Value v) {
        throw new RuntimeException("UnSupport Minus Function");
    }

    public Value divide(Value v) {
        throw new RuntimeException("UnSupport divide Function");
    }

    public Value multiply(Value v) {
        throw new RuntimeException("UnSupport multiply Function");
    }

    public Value modulus(Value v) {
        throw new RuntimeException("UnSupport modulus Function");
    }

    public int getSignum() {
        throw new RuntimeException("NOT Suppor SIGNUM");
    }

    public abstract String getSQL();

    static Value cache(Value v) {
        if (SysProperties.OBJECT_CACHE) {
            int hash = v.hashCode();
            if (softCache == null) {
                softCache = new SoftReference<Value[]>(null);
            }
            Value[] cache = softCache.get();
            if (cache == null) {
                cache = new Value[SysProperties.OBJECT_CACHE_SIZE];
                softCache = new SoftReference<Value[]>(cache);
            }
            int index = hash & (SysProperties.OBJECT_CACHE_SIZE - 1);
            Value cached = cache[index];
            if (cached != null) {
                if (cached.getType() == v.getType() && v.equals(cached)) {
                    // cacheHit++;
                    return cached;
                }
            }
            // cacheMiss++;
            // cache[cacheCleaner] = null;
            // cacheCleaner = (cacheCleaner + 1) & (Constants.OBJECT_CACHE_SIZE - 1);
            cache[index] = v;
        }
        return v;
    }
}
