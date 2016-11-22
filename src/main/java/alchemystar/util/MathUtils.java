package alchemystar.util;

/**
 * @Author lizhuyang
 */
public class MathUtils {

    public static int compareLong(long a, long b) {
        return a == b ? 0 : a < b ? -1 : 1;
    }

    public static int compareInt(int a, int b) {
        return a == b ? 0 : a < b ? -1 : 1;
    }
}
