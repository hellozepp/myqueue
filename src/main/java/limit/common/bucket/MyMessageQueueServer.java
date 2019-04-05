package limit.common.bucket;

import limit.common.queue.MqConfig;
import limit.common.sentinel.ControlRole;

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
        Socket s = null;
        //BufferedReader dl = null;
        TpsLimiterFilter dl = null;
        BufferedReader bufferedReader = null;
        try {
            ServerSocket ss = new ServerSocket();
            ss.setReceiveBufferSize(DEFAULT_RECEIVE_BUF_SIZE);
            ss.bind(new InetSocketAddress(DEFAULT_LOCAL_IP, DEFAULT_LOCAL_PORT));
            while (true) {
                try {
                    s = ss.accept();
                    System.out.println("[MyMessageQueueServer] start accept data in port:" + s.getPort() + "!");
                    dl = new TpsLimiterFilter(s.getInputStream(), new SlidingWindow(1024));
                    //dl = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    //get message head
                    bufferedReader = new BufferedReader(new InputStreamReader(dl));
                    String data = bufferedReader.readLine();
                    String topic = parseMessage(data);
                    int second = 10;
                    long timeCost = 0;
                    double dataLength = 0;
                    long a = System.nanoTime();
                    while ((data = bufferedReader.readLine()) != null) {
                        storeMessage(topic, data);
                        dataLength += data.length();
                        if ((timeCost = (System.nanoTime() - a) / 1000000000) >= second) {
                            second += 10;
                            System.out.println("[MyMessageQueueServer] " + new Date() + "," +
                                    " receive data length is " + (dataLength / (1048576))// 1024*1024
                                    + "M, time cost:" + timeCost + "s");
                        }
                    }
                    System.out.println("[MyMessageQueueServer] total message size is " + (dataLength / (1024 * 1024)) + "M,end ! "
                            + (System.nanoTime() - a) / 1000000000 + "s");
                } finally {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (dl != null) {
                        dl.close();
                    }
                    if (s != null) {
                        s.close();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void storeMessage(String topic, String data) {
        try {
            if (map.get(topic) != null) {
                map.get(topic).put(data);
            } else {
                LinkedBlockingQueue<String> strings = new LinkedBlockingQueue<>();

                strings.put(data);
                map.put(topic, strings);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("store message error!");
        }
    }

    private String parseMessage(String data) {
        String topic;
        if (data != null && data.length() != 0 && data.split(",").length >= 2) {
            String[] split = data.split(",");
            topic = split[1];
            return topic;
        }
        System.out.println("[MyMessageQueueServer] seq head error!");
        throw new IllegalArgumentException("seq head error!");
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
