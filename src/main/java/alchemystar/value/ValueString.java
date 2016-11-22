package alchemystar.value;

import alchemystar.util.RiderStringUtils;

/**
 * @Author lizhuyang
 */
public class ValueString extends Value {

    private static final ValueString EMPTY = new ValueString("");

    private final String value;

    protected ValueString(String value) {
        this.value = value;
    }

    @Override
    public int getType() {
        return Value.STRING;
    }

    @Override
    public String getString() {
        return value;
    }

    public static ValueString get(String s) {
        if (s.length() == 0) {
            return EMPTY;
        }
        return new ValueString(s);
        // todo cache
      /*  ValueString obj = new ValueString(StringCacheUtil.cache(s));
        if (s.length() > SysProperties.OBJECT_CACHE_MAX_PER_ELEMENT_SIZE) {
            return obj;
        }
        return (ValueString) Value.cache(obj);*/
    }

    @Override
    protected int compareSecure(Value o) {
        ValueString v = (ValueString) o;
        return value.compareTo(v.value);
    }

    public String getSQL() {
        return RiderStringUtils.quoteStringSQL(value);
    }
}
