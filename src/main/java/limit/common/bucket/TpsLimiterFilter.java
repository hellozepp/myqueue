package limit.common.bucket;


import java.io.IOException;
import java.io.InputStream;

/**
 * @Author: zhanglin
 * @Date: 2019/3/29
 * @Time: 9:13 PM
 */
public class TpsLimiterFilter extends InputStream {
    private InputStream is;
    private SlidingWindow slidingWindow;


    public TpsLimiterFilter(InputStream is, SlidingWindow slidingWindow) {
        super();
        this.is = is;
        this.slidingWindow = slidingWindow;
    }

    @Override
    public int read() throws IOException {
        if (slidingWindow != null) {
            slidingWindow.limitNextBytes();
        }
        return this.is.read();
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        if (slidingWindow != null) {
            //once operate is Package size
            slidingWindow.limitNextBytes(len);
        }
        return this.is.read(b, off, len);
    }
}