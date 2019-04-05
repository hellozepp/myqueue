package limit.common.bucket;

import com.google.common.util.concurrent.RateLimiter;
import limit.common.sentinel.ControlRole;

/**
 * @Author: zhanglin
 * @Date: 2019/3/29
 * @Time: 9:13 PM
 */
public class SlidingWindow {

    /**
     * KB
     */
    private static int KB = 1024;

    /**
     * The smallest count window chunk length in bytes
     */
    public static Long WINDOW_LENGTH = 1024L;

    /**
     * How many bytes will be sent or receive
     */
    private int ackBytes = 0;

    /**
     * When the last piece was sent or receive
     */
    private long recentFinishTime = System.nanoTime();

    //use the guava ControlRole to control sliding window
    private ControlRole controlRole;

    /**
     * Default rate is 1024KB/s
     */
    private int maxRate = 1024;

    private static final int DEFAULT_MAX_RATE = 1024;

    /**
     * Time cost for sending WINDOW_LENGTH bytes in nanoseconds
     */
    private long timeCostPerChunk = (1000000000L * WINDOW_LENGTH) / (this.maxRate * KB);

    //the package max size (Bytes)
    private static final int DEFAULT_PACKAGE_SIZE = 8192;

    private static final int DEFAULT_BUCKET_NUM = DEFAULT_MAX_RATE * KB / DEFAULT_PACKAGE_SIZE;

    //use the sentinel ControlRole to control sliding window
    private RateLimiter producerRate;

    private int bucketNum;

    public int getBucketNum() {
        return bucketNum;
    }

    public void setBucketNum(int bucketNum) {
        this.bucketNum = bucketNum;
    }

    public SlidingWindow(int maxRate) {
        this.setMaxRate(maxRate);
        this.controlRole = new ControlRole();
        controlRole.initFlowControlRule(this.getBucketNum());

    }

    private synchronized void setMaxRate(int maxRate) throws IllegalArgumentException {
        if (maxRate < 0) {
            throw new IllegalArgumentException("maxRate can not less than 0");
        }
        this.maxRate = maxRate;
        bucketNum = maxRate * KB / DEFAULT_PACKAGE_SIZE;
        System.out.println("[setMaxRate] bucketNum:" + bucketNum);
        if (bucketNum < 0) {
            producerRate = RateLimiter.create(DEFAULT_BUCKET_NUM);
        } else {
            producerRate = RateLimiter.create(bucketNum);
        }
        if (maxRate == 0) {
            this.timeCostPerChunk = 0;
        } else {
            this.timeCostPerChunk = (1000000000L * WINDOW_LENGTH) / (this.maxRate * KB);
        }
    }

    /**
     * Next len bytes should do SlidingWindow limit
     */
    synchronized void limitNextBytes() {
        this.ackBytes += 1;
        doLimit();
    }

    private void doLimit() {
        producerRate.acquire();
    }

    private void doLimitWindow() {
        while (this.ackBytes > WINDOW_LENGTH) {
            long nowTick = System.nanoTime();
            long missedTime = this.timeCostPerChunk - (nowTick - this.recentFinishTime);
            if (missedTime > 0) {
                try {
                    Thread.sleep(missedTime / 1000000, (int) (missedTime % 1000000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.ackBytes -= WINDOW_LENGTH;
            this.recentFinishTime = nowTick + (missedTime > 0 ? missedTime : 0);
        }
    }

    synchronized void limitNextBytes(int len) {
        this.ackBytes += len;
//        doLimitWindow();
        ControlRole.doLimitSentinel(controlRole.getKEY());
    }
}