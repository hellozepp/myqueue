package banwith2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * @Author: zhanglin
 * @Date: 2019/3/29
 * @Time: 9:13 PM
 */
public class MyConsumerRunner implements Runnable {

    public MyConsumerRunner(MqConfig config) {
        this.config = config;
    }

    private final MqConfig config;
    private static final int DEFAULT_RECEIVE_BUF_SIZE = 8192;
    private static final int DEFAULT_PORT = 9976;
    private static final int DEFAULT_TIMEOUT_MS = 1000 * 60 * 60;

    public void receive() {
        Socket socket = null;
        PrintStream printStream = null;
        String line;
        try {
            socket = new Socket();
            socket.setReceiveBufferSize(DEFAULT_RECEIVE_BUF_SIZE);
            socket.setReuseAddress(true);
            socket.setSoTimeout(DEFAULT_TIMEOUT_MS);
            socket.bind(new InetSocketAddress(DEFAULT_PORT));
            socket.connect(new InetSocketAddress(config.getServerIp(), config.getServerPort()));
            System.out.println("MyConsumerRunner has started : " + socket.getInetAddress().toString() + ":" + socket.getLocalPort() + "...");
            printStream = new PrintStream(socket.getOutputStream());
            printStream.println(MqConfig.CONSUMER_ROLE_FLAG + "," + config.getTopic());
            printStream.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while ((line = br.readLine()) != null) {
                System.out.println("line size:" + line.length());
            }
            System.out.println("MyConsumerRunner send msg success!");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (printStream != null) {
                    printStream.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("close IO exception!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        this.receive();
    }
}
