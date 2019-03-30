package banwith2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author: zhanglin
 * @Date: 2019/3/29
 * @Time: 9:13 PM
 */
public class MyMessageQueueServer implements Runnable {
    private static ConcurrentHashMap<String, LinkedBlockingQueue<String>> map = new ConcurrentHashMap<>();
    private static final int DEFAULT_RECEIVE_BUF_SIZE = 8192;
    private static final int DEFAULT_LOCAL_PORT = 30000;
    private static final String DEFAULT_LOCAL_IP = "127.0.0.1";

    private void testReceiver() throws Exception {
        System.out.println("[MyMessageQueueServer] MyMessageQueueServer is running...");
        Socket s;
//        BufferedReader dl = null;
        TpsLimiterFilter dl;
        try {
            ServerSocket ss = new ServerSocket();
            ss.setReceiveBufferSize(DEFAULT_RECEIVE_BUF_SIZE);
            ss.bind(new InetSocketAddress(DEFAULT_LOCAL_IP, DEFAULT_LOCAL_PORT));
            while (true) {
                s = ss.accept();
                System.out.println("[MyMessageQueueServer] start accept data in port:" + s.getPort() + "!");
                dl = new TpsLimiterFilter(new BufferedReader(new InputStreamReader(s.getInputStream())),
                        new SlidingWindow(1024));
//                dl = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String data;
                String flag = null;
                String topic = null;
                int second = 10;
                long timeCost, dataLength = 0, lineCount = 0;
                long a = System.nanoTime();
                while ((data = dl.readLine()) != null) {
                    if (lineCount == 0l) {
                        lineCount++;
                        if (data.length() == 0 || data.split(",").length < 2) {
                            System.out.println("[MyMessageQueueServer] seq head error!");
                            break;
                        }
                        String[] split = data.split(",");
                        flag = split[0];
                        topic = split[1];
                        if (MqConfig.CONSUMER_ROLE_FLAG.toString().equals(flag)) {
                            take(topic, s);
                        }
                        continue;
                    }
                    if (map.get(topic) != null) {
                        map.get(topic).put(data);
                    } else {
                        LinkedBlockingQueue<String> strings = new LinkedBlockingQueue<>();
                        strings.put(data);
                        map.put(topic, strings);
                    }
                    System.out.println(data);
                    if ((timeCost = (System.nanoTime() - a) / 1000000000) >= second) {
                        second += 10;
                        System.out.println(new Date() + ",role flag:" + flag + ", receive data size is " + (dataLength += data.length()) + ", time cost:" + timeCost + "s");
                    }
                }
                System.out.println("[MyMessageQueueServer] end ! " + (System.nanoTime() - a) / 1000000000 + "s");
                if (dl != null) {
                    dl.close();
                }
                if (s != null) {
                    s.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void take(String topic, Socket s) {
        new Thread(() -> {
            try {
                PrintStream printStream = new PrintStream(s.getOutputStream());
                //TODO there need use observer
                while (true) {
                    //this take process is blocking
                    printStream.print(map.get(topic).take());
                    printStream.flush();
                }
            } catch (IOException e) {
                System.out.println("[MyMessageQueueServer] close IO exception!");
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

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
