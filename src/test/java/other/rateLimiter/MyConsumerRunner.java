package other.rateLimiter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
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
            socket = new Socket("127.0.0.1", 30000);
            socket.setSendBufferSize(1024 * 1024);
            PrintStream ps = new PrintStream(socket.getOutputStream());
            ps.println("谢谢祝福！");
            ps.flush();
            // 将Socket对应的输入流包装成BufferedReader
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            // 进行普通IO操作
            String line = br.readLine();
            System.out.println("来自服务器的数据：" + line);

            ps.close();
            // 关闭输入流、socket
            br.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 128; i++) {

        }
    }
}
