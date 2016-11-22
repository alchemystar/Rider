package alchemystar.util;

import java.lang.ref.SoftReference;

import alchemystar.constant.SysProperties;

/**
 * @Author lizhuyang
 */
public class StringCacheUtil {
    private static SoftReference<String[]> softCache = new SoftReference<String[]>(null);
    private static long softCacheCreated;
    private static final char[] HEX = "0123456789abcdef".toCharArray();
    private static final int[] HEX_DECODE = new int['f' + 1];

    static {
        for (int i = 0; i < HEX_DECODE.length; i++) {
            HEX_DECODE[i] = -1;
        }
        for (int i = 0; i <= 9; i++) {
            HEX_DECODE[i + '0'] = i;
        }
        for (int i = 0; i <= 5; i++) {
            HEX_DECODE[i + 'a'] = HEX_DECODE[i + 'A'] = i + 10;
        }
    }

    private StringCacheUtil() {
        // utility class
    }

    private static String[] getCache() {
        String[] cache;
        // softCache can be null due to a Tomcat problem
        // a workaround is disable the system property org.apache.
        // catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES
        if (softCache != null) {
            cache = softCache.get();
            if (cache != null) {
                return cache;
            }
        }
        // create a new cache at most every 5 seconds
        // so that out of memory exceptions are not delayed
        long time = System.currentTimeMillis();
        if (softCacheCreated != 0 && time - softCacheCreated < 5000) {
            return null;
        }
        try {
            cache = new String[SysProperties.OBJECT_CACHE_SIZE];
            softCache = new SoftReference<String[]>(cache);
            return cache;
        } finally {
            softCacheCreated = System.currentTimeMillis();
        }
    }

    public static String cache(String s) {
        if (!SysProperties.OBJECT_CACHE) {
            return s;
        }
        if (s == null) {
            return s;
        } else if (s.length() == 0) {
            return "";
        }
        int hash = s.hashCode();
        String[] cache = getCache();
        if (cache != null) {
            int index = hash & (SysProperties.OBJECT_CACHE_SIZE - 1);
            String cached = cache[index];
            if (cached != null) {
                if (s.equals(cached)) {
                    return cached;
                }
            }
            cache[index] = s;
        }
        return s;
    }
}
