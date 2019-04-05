package other.bucket;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.lang.Thread.currentThread;

/**
 * ${DESCRIPTION}
 *
 * @author mengxp
 * @version 1.0
 * @create 2018-01-20 23:11
 * 漏桶算法测试
 * 实现漏桶算法 实现多线程生产者消费者模型 限流
 **/
public class BuckerTest {

    public static void main(String[] args) {
        final Bucket bucket = new Bucket();
        final AtomicInteger DATA_CREATOR = new AtomicInteger(0);

        //生产线程 10个线程 每秒提交 50个数据  1/0.2s*10=50个
        IntStream.range(0, 10).forEach(i -> {
            new Thread(() -> {
                for (; ; ) {
                    int data = DATA_CREATOR.incrementAndGet();
                    try {
                        bucket.submit(data);
                        TimeUnit.MILLISECONDS.sleep(200);
                    } catch (Exception e) {
                        //对submit时，如果桶满了可能会抛出异常
                        if (e instanceof IllegalStateException) {
                            System.out.println(e.getMessage());
                            //当满了后，生产线程就休眠1分钟
                            try {
                                TimeUnit.SECONDS.sleep(60);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        });


        //消费线程  采用RateLimiter每秒处理10个  综合的比率是5:1
        IntStream.range(0, 10).forEach(i -> {
            new Thread(
                    () -> {
                        for (; ; ) {
                            bucket.takeThenConsumer(x -> {
                                System.out.println(currentThread() + "C.." + x);
                            });
                        }
                    }
            ).start();
        });

    }
}