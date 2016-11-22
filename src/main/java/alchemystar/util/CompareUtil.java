package alchemystar.util;

import alchemystar.value.Value;

/**
 * @Author lizhuyang
 */
public class CompareUtil {

    public static boolean areEqual(Value a, Value b) {
        return a.compareTo(b) == 0;
    }

    public static int compare(Value a, Value b) {
        return a.compareTo(b);
    }

    public static boolean equalsIdentifiers(String a, String b) {
        if (a == b || a.equals(b)) {
            return true;
        }
        // 默认开启ignoreCase
        if (a.equalsIgnoreCase(b)) {
            return true;
        }
        return false;
    }

}
