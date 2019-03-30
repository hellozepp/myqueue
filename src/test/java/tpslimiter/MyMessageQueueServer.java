//package tpslimiter;
//
//import banwith.BandwidthLimiter;
//import banwith.DownloadLimiter;
//
//import java.io.*;
//import java.net.InetSocketAddress;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.nio.ByteBuffer;
//import java.util.concurrent.LinkedBlockingDeque;
//import java.util.stream.Stream;
//
///**
// * @Author: zhanglin
// * @Date: 2019/3/30
// * @Time: 6:16 PM
// */
//public class MyMessageQueueServer implements Runnable {
//    private LinkedBlockingDeque<Stream<String>> linkedBlockingDeque = new LinkedBlockingDeque<>();
//
//    public void testReceiver() throws Exception {
//        System.out.println("++++++++++");
//        Socket s = null;
//        try {
//            ServerSocket ss = new ServerSocket();
//            ss.setReceiveBufferSize(1);
//            ss.bind(new InetSocketAddress("localhost", 30000));
//            while (true) {
//                System.out.println("++++++++++");
//                s = ss.accept();
//                DownloadLimiter dl = new DownloadLimiter(s.getInputStream(), new BandwidthLimiter(1024));
//                DataInputStream is = new DataInputStream(dl);
//                int len = is.readInt();
//                ByteBuffer buffer = ByteBuffer.allocate(4 + len);
//                buffer.putInt(len);
//                is.readFully(buffer.array(), 4, buffer.remaining());
//
//
//                System.out.println("start accept port:" + s.getLocalPort() + " data!");
//                PrintStream ps = new PrintStream(s.getOutputStream());
//                ps.println("您好，您收到了服务器的新年祝福！");
//                ps.flush();//网卡不刷 等凑够包大小再发
//                BufferedReader br = new BufferedReader(
//                        new InputStreamReader(s.getInputStream()));
//                Stream<String> lines = br.lines();
//                linkedBlockingDeque.add(lines);
//                br.close();
//                ps.close();
//            }
//        } finally {
//            if (s != null) {
//                s.close();
//            }
//        }
//    }
//
//    @Override
//    public void run() {
//        try {
//            testReceiver();
//        } catch (IOException e) {
//            System.out.println("[MyMessageQueueServer] init the ServerSocketChannel error:" + e.getMessage());
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//}
