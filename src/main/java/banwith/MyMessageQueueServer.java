package banwith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
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
        System.out.println("MyMessageQueueServer is running...");
        Socket s = null;
        try {
            ServerSocket ss = new ServerSocket();
            ss.setReceiveBufferSize(8192);
            ss.bind(new InetSocketAddress("127.0.0.1", 30000));
            while (true) {
                s = ss.accept();
                System.out.println("start accept data in port:" + s.getLocalPort() + "!");
                DownloadLimiter dl = new DownloadLimiter(new BufferedReader(new InputStreamReader(s.getInputStream())),
                        new BandwidthLimiter(1024));
//                TODO
                String data;
                long a = System.nanoTime();
                while ((data = dl.readLine()) != null) {
                    System.out.println(new Date() + " receive data size is " + data.length());
                }
                System.out.println("end !!!!!! " + (System.nanoTime() - a) / 1000000000 + "s");
                PrintStream ps = new PrintStream(s.getOutputStream());
                ps.println("ok");
                ps.flush();//网卡不刷 等凑够包大小再发
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(s.getInputStream()));
                Stream<String> lines = br.lines();
                linkedBlockingDeque.add(lines);
                dl.close();
                br.close();
                ps.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public static void main(String[] args) {
        new MyMessageQueueServer().run();
    }
}
