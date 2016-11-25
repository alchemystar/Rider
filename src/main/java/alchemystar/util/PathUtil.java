package alchemystar.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author lizhuyang
 */
public class PathUtil {

    public static final int INIT_STATUS = 0;
    public static final int PERCENT_READ = 1;
    public static final int D_READ = 2;
    public static final int LEFT_BRACE_READ = 3;
    public static final int RIGHT_BRACE_READ = 4;

    public static String renderPath(String path) {
        // 最多支持512长度的路径
        StringBuilder result = new StringBuilder();
        int status = INIT_STATUS;
        StringBuilder timeFormat = new StringBuilder();
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            // state machine
            // 每次状态流转都会skip掉当前的char
            switch (c) {
                case '%':
                    switch (status) {
                        case INIT_STATUS:
                            status = PERCENT_READ;
                            continue;
                        default:
                            status = INIT_STATUS;
                    }
                case 'd':
                    switch (status) {
                        case PERCENT_READ:
                            status = D_READ;
                            continue;
                        case LEFT_BRACE_READ:
                            break;
                        default:
                            status = INIT_STATUS;
                    }
                    break;
                case '{':
                    switch (status) {
                        case D_READ:
                            status = LEFT_BRACE_READ;
                            continue;
                        default:
                            status = INIT_STATUS;
                    }
                    break;
                case '}':
                    switch (status) {
                        case LEFT_BRACE_READ:
                            status = RIGHT_BRACE_READ;
                            // 这边不continue,需要进行下面的动作
                            break;
                        default:
                            status = INIT_STATUS;
                    }
                    break;
                default:
                    // 如果读到了其它字符,则根据当前的status进行流转
                    switch (status) {
                        case LEFT_BRACE_READ:
                            break;
                        default:
                            status = INIT_STATUS;
                            break;
                    }
                    break;
            }
            switch (status) {
                case LEFT_BRACE_READ:
                    timeFormat.append(c);
                    break;
                case RIGHT_BRACE_READ:
                    // 在此进行渲染
                    SimpleDateFormat sdf = new SimpleDateFormat(timeFormat.toString());
                    timeFormat = new StringBuilder();
                    // 并加到result字符串里面
                    result.append(sdf.format(new Date()));
                    break;
                default:
                    result.append(c);
                    break;
            }
        }
        return result.toString();

    }

    public static void main(String args[]) {
        // String rendered = renderPath("/home/work/yyyyMMdd");
        String rendered = renderPath("/home/work/%d{yyyyMMdd}/hahaha/js_fund_file_%d{yyyy-MM-dd|HH:mm:ss}");
        System.out.println(rendered);
    }
}
