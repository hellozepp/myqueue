package tpslimiter;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Author: zhanglin
 * @Date: 2019/3/30
 * @Time: 6:16 PM
 */
public class TpsLimitFilter extends InputStream {
//    public static String TPS_LIMIT_RATE_KEY
    @Override
    public int read() throws IOException {
        return 0;
    }
}
