package limit.common;

/**
 * @Author: zhanglin
 * @Date: 2019/3/29
 * @Time: 9:13 PM
 */
public class SlidingWindow {

    /**
     * KB
     */
    private static Long KB = 1024L;

    /**
     * The smallest count window chunk length in bytes
     */
    private static Long WINDOW_LENGTH = 1024L;

    /**
     * How many bytes will be sent or receive
     */
    private int ackBytes = 0;

    /**
     * When the last piece was sent or receive
     */
    private long recentFinishTime = System.nanoTime();

    /**
     * Default rate is 1024KB/s
     */
    private int maxRate = 1024;

    /**
     * Time cost for sending WINDOW_LENGTH bytes in nanoseconds
     */
    private long timeCostPerChunk = (1000000000L * WINDOW_LENGTH) / (this.maxRate * KB);

    public SlidingWindow(int maxRate) {
        this.setMaxRate(maxRate);
    }

    private synchronized void setMaxRate(int maxRate) throws IllegalArgumentException {
        if (maxRate < 0) {
            throw new IllegalArgumentException("maxRate can not less than 0");
        }
        this.maxRate = maxRate;
        if (maxRate == 0) {
            this.timeCostPerChunk = 0;
        } else {
            this.timeCostPerChunk = (1000000000L * WINDOW_LENGTH) / (this.maxRate * KB);
        }
    }

    /**
     * Next len bytes should do SlidingWindow limit
     *
     * @param len
     */
    synchronized void limitNextBytes(int len) {
        this.ackBytes += len;

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
}