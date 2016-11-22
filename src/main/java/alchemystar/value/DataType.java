package alchemystar.value;

/**
 * @Author lizhuyang
 */
public class DataType {

    public static int getTypeByName(String name) {
        name = name.toUpperCase();
        if ("LONG".equals(name) || "BIGINT".equals(name)) {
            return Value.LONG;
        }
        if ("STRING".equals(name) || "CHAR".equals(name) || "VARCHAR".equals(name)) {
            return Value.STRING;
        }
        if ("BOOLEAN".equals(name)) {
            return Value.BOOLEAN;
        }
        if ("INT".equals(name)) {
            return Value.INT;
        }
        if ("BYTE".equals(name)) {
            return Value.BYTE;
        }
        throw new RuntimeException("Not Support This Type:" + name);
    }

    public static String getTypeName(int type) {

        if (type == Value.BOOLEAN) {
            return "BOOLEAN";
        }
        if (type == Value.ARRAY) {
            return "ARRAY";
        }
        if (type == Value.STRING) {
            return "STRING";
        }
        if (type == Value.LONG) {
            return "LONG";
        }
        if (type == Value.INT) {
            return "INT";
        }
        return "UNKNOWN";

    }
}
