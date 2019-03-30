package qpslimit;

/**
 * @Author: zhanglin
 * @Date: 2019/3/30
 * @Time: 12:16 PM
 */
public class TestMyQueue {
    public static void main(String[] args) {
        new TestMyQueue().call();
    }

    public void call() {
        new MyMessageQueueServer().run();
        new MyProducerRunner().run();
        new MyConsumerRunner().run();
    }
}
