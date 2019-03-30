package banwith2;


import java.io.BufferedReader;
import java.io.IOException;

/**
 * @Author: zhanglin
 * @Date: 2019/3/29
 * @Time: 9:13 PM
 */
public class TpsLimiterFilter extends BufferedReader {
    private BufferedReader is = null;
    private SlidingWindow slidingWindow = null;

    @Override
    public String readLine() throws IOException {
        String s = is.readLine();
        if (slidingWindow != null && s != null)
            slidingWindow.limitNextBytes(s.length());
        return s;
    }


    public TpsLimiterFilter(BufferedReader is, SlidingWindow slidingWindow) {
        super(is);
        this.is = is;
        this.slidingWindow = slidingWindow;
    }

}
 /**
     * @Override public int read() throws IOException {
     * if (this.slidingWindow != null) {
     * this.slidingWindow.limitNextBytes();
     * }
     * return this.is.read();
     * }
     * public int read(byte b[], int off, int len) throws IOException {
     * if (slidingWindow != null)
     * slidingWindow.limitNextBytes(len);
     * return this.is.read(b, off, len);
     * }
     */