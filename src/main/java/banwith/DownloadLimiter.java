package banwith;


import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Le
 */
public class DownloadLimiter extends BufferedReader {
    private BufferedReader reader = null;
    private BandwidthLimiter bandwidthLimiter = null;

    @Override
    public String readLine() throws IOException {
        String s = super.readLine();
        if (bandwidthLimiter != null && s!=null)
            bandwidthLimiter.limitNextBytes(s.length());
        return s;
    }

    public DownloadLimiter(BufferedReader reader, BandwidthLimiter bandwidthLimiter) {
        super(reader);
        this.reader = reader;
        this.bandwidthLimiter = bandwidthLimiter;
    }

}
