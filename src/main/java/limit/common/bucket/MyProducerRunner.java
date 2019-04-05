package limit.common.bucket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @Author: zhanglin
 * @Date: 2019/3/29
 * @Time: 9:13 PM
 */
public class MyProducerRunner implements Runnable {

    private InputStream inputStream;
    private BufferedReader br;
    private final MqConfig config;
    private static final int DEFAULT_SEND_BUF_SIZE = 8192;
    private static final int DEFAULT_PORT = 19961;

    public MyProducerRunner(MqConfig config, InputStream inputStream) {
        this.config = config;
        this.inputStream = inputStream;
    }

    public MyProducerRunner(MqConfig config, BufferedReader br) {
        this.config = config;
        this.br = br;
    }

    public void send() {
        Socket socket = null;
        PrintStream printStream = null;
        String str;
        try {
            socket = new Socket();
            socket.setSendBufferSize(DEFAULT_SEND_BUF_SIZE);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(DEFAULT_PORT));
            socket.connect(new InetSocketAddress(config.getServerIp(), config.getServerPort()));
            System.out.println("MyProducerRunner has started : " + socket.getInetAddress().toString() + ":" + socket.getLocalPort() + "...");

            printStream = new PrintStream(socket.getOutputStream());
            printStream.println(limit.common.queue.MqConfig.PRODUCER_ROLE_FLAG + "," + config.getTopic());
            while ((str = br.readLine()) != null) {
                printStream.println(str);
            }
            printStream.flush();
            System.out.println("MyProducerRunner send msg success!");
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

    public void sendBytes() {
        Socket socket = null;
        PrintStream printStream = null;
        String str;
        try {
            socket = new Socket();
            socket.setSendBufferSize(DEFAULT_SEND_BUF_SIZE);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(DEFAULT_PORT));
            socket.connect(new InetSocketAddress(config.getServerIp(), config.getServerPort()));
            System.out.println("MyProducerRunner has started : " + socket.getInetAddress().toString() + ":" + socket.getLocalPort() + "...");

            printStream = new PrintStream(socket.getOutputStream());
            printStream.println(MqConfig.PRODUCER_ROLE_FLAG + "," + config.getTopic());
            byte[] b = new byte[DEFAULT_SEND_BUF_SIZE];
            int hasRead;
            long counter = 0;
            while ((hasRead = inputStream.read(b)) > 0) {
                counter += hasRead;
                printStream.write(b, 0, hasRead);
            }
            printStream.flush();
            System.out.println("MyProducerRunner send msg success! counter:" + counter);
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
//        this.send();
        sendBytes();
    }
}
