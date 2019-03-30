package banwith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @Author: zhanglin
 * @Date: 2019/3/30
 * @Time: 12:49 AM
 * 数据报限流
 * 题目内容:创建队列，生产者生产数据放入队列，消费者消费数据读取队列。
 * 并且对生产者进行限流，以获得1M/s的数据处理速率。java实现
 */
public class MyConsumerRunner implements Runnable {

    @Override
    public void run() {
        Socket socket;
        try {
            socket = new Socket();
            socket.bind(new InetSocketAddress(30001));
            socket.connect(new InetSocketAddress("127.0.0.1", 30000));
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MyConsumerRunner().run();
    }
}
