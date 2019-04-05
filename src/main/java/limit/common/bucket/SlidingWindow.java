package limit.common.bucket;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.google.common.util.concurrent.Monitor;
import com.google.common.util.concurrent.RateLimiter;
import limit.common.sentinel.ControlRole;

import java.util.concurrent.atomic.AtomicInteger;

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
    private static Long WINDOW_LENGTH = 1024L;
    private ControlRole controlRole;

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
    private static final int DEFAULT_MAX_RATE = 1024;
    /**
     * Time cost for sending WINDOW_LENGTH bytes in nanoseconds
     */
    private long timeCostPerChunk = (1000000000L * WINDOW_LENGTH) / (this.maxRate * KB);
    //定义桶的大小 Bytes
    private static final int DEFAULT_PACKAGE_SIZE = 8192;
    private static final int DEFAULT_BUCKET_NUM = DEFAULT_MAX_RATE * KB / DEFAULT_PACKAGE_SIZE;
    private AtomicInteger curBucketSize = new AtomicInteger();
    //消费者 不论多少个线程，每秒最大的处理能力是1秒中执行10次
    private RateLimiter producerRate;
    private int bucketNum;
    //往桶里面放数据时，确认没有超过桶的最大的容量
    private Monitor offerMonitor = new Monitor();

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

    private void doLimitSentinel() {
        Entry entry = null;
        try {
            ContextUtil.enter(controlRole.getKEY());
            entry = SphU.entry(controlRole.getKEY(), EntryType.OUT);

            // Your business logic here.
        } catch (BlockException ex) {
            // Blocked.
            System.out.printf("[%d] Blocked!\n", System.currentTimeMillis());
        } finally {
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }
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
        doLimitSentinel();
    }
}