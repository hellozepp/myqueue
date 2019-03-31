package limit.common;

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
    private static final int DEFAULT_LOCAL_CONSUME_PORT = 30003;
    private static final String DEFAULT_LOCAL_IP = "127.0.0.1";

    private void receiver() throws Exception {
        System.out.println("[MyMessageQueueServer] receiver is running...");
        Socket s;
        /**BufferedReader dl = null;*/
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
                /**dl = new BufferedReader(new InputStreamReader(s.getInputStream()));*/
                String data;
                String flag = null;
                String topic = null;
                int second = 10;
                long timeCost, dataLength = 0, lineCount = 0;
                long a = System.nanoTime();
                while ((data = dl.readLine()) != null) {
                    if (lineCount == 0L) {
                        lineCount++;
                        if (data.length() == 0 || data.split(",").length < 2) {
                            System.out.println("[MyMessageQueueServer] seq head error!");
                            break;
                        }
                        String[] split = data.split(",");
                        flag = split[0];
                        topic = split[1];
                        continue;
                    }
                    if (map.get(topic) != null) {
                        map.get(topic).put(data);
                    } else {
                        LinkedBlockingQueue<String> strings = new LinkedBlockingQueue<>();
                        strings.put(data);
                        map.put(topic, strings);
                    }
                    if ((timeCost = (System.nanoTime() - a) / 1000000000) >= second) {
                        second += 10;
                        System.out.println("[MyMessageQueueServer] " + new Date() + ",role flag:" + flag + "," +
                                " receive data length is " + (dataLength += data.length())
                                + ", time cost:" + timeCost + "s");
                    }
                }
                System.out.println("[MyMessageQueueServer] end ! " + (System.nanoTime() - a) / 1000000000 + "s");
                dl.close();
                s.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void take() {
        System.out.println("[MyMessageQueueServer] taker is running...");
        Socket s = null;
        PrintStream printStream = null;
        BufferedReader dl;
        String topic;
        try {
            ServerSocket ss = new ServerSocket();
            ss.setReceiveBufferSize(DEFAULT_RECEIVE_BUF_SIZE);
            ss.bind(new InetSocketAddress(DEFAULT_LOCAL_IP, DEFAULT_LOCAL_CONSUME_PORT));
            s = ss.accept();
            System.out.println("[MyMessageQueueServer] start accept data in port:" + s.getPort() + "!");
            dl = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String data = dl.readLine();
            /**SYMBOL_SIZE : 2 , SYMBOL_BITS : ","*/
            if (data.length() == 0 || data.split(MqConfig.SYMBOL_BITS).length < MqConfig.SYMBOL_SIZE) {
                System.out.println("[MyMessageQueueServer] seq head error!");
            } else {
                //TODO there need use observer
                while (true) {
                    String[] split = data.split(",");
                    topic = split[1];
                    printStream = new PrintStream(s.getOutputStream());
                    //this take process is blocking
                    if (map.get(topic) != null) {
                        String take = map.get(topic).take();
                        printStream.println(take);
                        printStream.flush();
                        continue;
                    }
                    Thread.sleep(500);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (printStream != null) {
                printStream.close();
            }
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            receiver();
        } catch (IOException e) {
            System.out.println("[MyMessageQueueServer] init the ServerSocketChannel error:" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
