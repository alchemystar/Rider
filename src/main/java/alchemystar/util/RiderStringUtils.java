package alchemystar.util;

/**
 * @Author lizhuyang
 */
public class RiderStringUtils {

    public static String quoteStringSQL(String s) {
        if (s == null) {
            return "NULL";
        }
        int length = s.length();
        StringBuilder buff = new StringBuilder(length + 2);
        buff.append('\'');
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                buff.append(c);
            } else if (c < ' ' || c > 127) {
                // need to start from the beginning because maybe there was a \
                // that was not quoted
                return "STRINGDECODE(" + quoteStringSQL(javaEncode(s)) + ")";
            }
            buff.append(c);
        }
        buff.append('\'');
        return buff.toString();
    }

    public static String javaEncode(String s) {
        int length = s.length();
        StringBuilder buff = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                //            case '\b':
                //                // BS backspace
                //                // not supported in properties files
                //                buff.append("\\b");
                //                break;
                case '\t':
                    // HT horizontal tab
                    buff.append("\\t");
                    break;
                case '\n':
                    // LF linefeed
                    buff.append("\\n");
                    break;
                case '\f':
                    // FF form feed
                    buff.append("\\f");
                    break;
                case '\r':
                    // CR carriage return
                    buff.append("\\r");
                    break;
                case '"':
                    // double quote
                    buff.append("\\\"");
                    break;
                case '\\':
                    // backslash
                    buff.append("\\\\");
                    break;
                default:
                    int ch = c & 0xffff;
                    if (ch >= ' ' && (ch < 0x80)) {
                        buff.append(c);
                        // not supported in properties files
                        // } else if(ch < 0xff) {
                        // buff.append("\\");
                        // // make sure it's three characters (0x200 is octal 1000)
                        // buff.append(Integer.toOctalString(0x200 | ch).substring(1));
                    } else {
                        buff.append("\\u");
                        String hex = Integer.toHexString(ch);
                        // make sure it's four characters
                        for (int len = hex.length(); len < 4; len++) {
                            buff.append('0');
                        }
                        buff.append(hex);
                    }
            }
        }
        return buff.toString();
    }

    public static String quoteIdentifier(String s) {
        int length = s.length();
        StringBuilder buff = new StringBuilder(length + 2);
        buff.append('\"');
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c == '"') {
                buff.append(c);
            }
            buff.append(c);
        }
        return buff.append('\"').toString();
    }

}
