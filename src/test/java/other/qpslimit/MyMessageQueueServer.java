package other.qpslimit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Stream;

/**
 * @Author: zhanglin
 * @Date: 2019/3/29
 * @Time: 9:13 PM
 */
public class MyMessageQueueServer implements Runnable {
    private LinkedBlockingDeque<Stream<String>> linkedBlockingDeque = new LinkedBlockingDeque<>();

    public void testReceiver() throws Exception {
        System.out.println("++++++++++");
        Socket s = null;
        try {
            ServerSocket ss = new ServerSocket();
            ss.setReceiveBufferSize(1);
            ss.bind(new InetSocketAddress("localhost", 30000));//连接远程服务端接口
            // 采用循环不断接受来自客户端的请求
            while (true) {
                System.out.println("++++++++++");
                s = ss.accept();
                System.out.println("start accept port:" + s.getLocalPort() + " data!");
                PrintStream ps = new PrintStream(s.getOutputStream());
                ps.println("您好，您收到了服务器的新年祝福！");
                ps.flush();//网卡不刷 等凑够包大小再发
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(s.getInputStream()));
                Stream<String> lines = br.lines();
                linkedBlockingDeque.add(lines);
                br.close();
                ps.close();
            }
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }

    @Override
    public void run() {
        try {
            testReceiver();
        } catch (IOException e) {
            System.out.println("[MyMessageQueueServer] init the ServerSocketChannel error:" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
