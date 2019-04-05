package sample;

import limit.common.bucket.MqConfig;
import limit.common.bucket.MyConsumerRunner;
import limit.common.bucket.MyMessageQueueServer;
import limit.common.bucket.MyProducerRunner;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: zhanglin
 * @Date: 2019/3/29
 * @Time: 9:13 PM
 */
public class BucketMqTest {
    public static ThreadFactory threadFactory;
    public static ThreadPoolExecutor threadPoolExecutor;

    @Before
    public void init() {
        threadFactory = new NameTreadFactory();
        threadPoolExecutor = new ThreadPoolExecutor(4, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), threadFactory);
    }

    @Test
    public void serverTest() {
        MyMessageQueueServer myMessageQueueServer = new MyMessageQueueServer();
        BucketMqTest.threadPoolExecutor.execute(myMessageQueueServer);
        BucketMqTest.threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                myMessageQueueServer.take();
            }
        });
        try {
            /**blocking the main thread*/
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void clientTest() {
        MqConfig config = new MqConfig("topic01", "127.0.0.1", 30000);
        MqConfig config1 = new MqConfig("topic01", "127.0.0.1", 30003);

        FileReader fileReader = null;
        BufferedReader br = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File("/Users/docker/Documents/testPack/hello.zepp.mrdemo-1.0-SNAPSHOT-jar-with-dependencies.jar"));
//            MyProducerRunner myProducerRunner = new MyProducerRunner(config, new BufferedReader(new FileReader(new File("/Users/docker/Documents/testPack/hello.zepp.mrdemo-1.0-SNAPSHOT-jar-with-dependencies.jar"))));
            MyProducerRunner myProducerRunner = new MyProducerRunner(config, fileInputStream);
            MyConsumerRunner myConsumerRunner = new MyConsumerRunner(config1);
            threadPoolExecutor.execute(myProducerRunner);
            threadPoolExecutor.execute(myConsumerRunner);

            try {
                /**blocking the main thread*/
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println("file path error!");
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                System.out.println("close IO exception!");
                e.printStackTrace();
            }
        }

    }

    static class NameTreadFactory implements ThreadFactory {

        private final AtomicInteger mThreadNum = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "[my-thread-" + mThreadNum.getAndIncrement() + "]");
            System.out.println(t.getName() + " has been created!");
            return t;
        }
    }

}
