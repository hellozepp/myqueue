package banwith2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @Author: zhanglin
 * @Date: 2019/3/29
 * @Time: 9:13 PM
 */
public class MyProducerRunner implements Runnable{

    private BufferedReader br;
    private final MqConfig config;
    private static final int DEFAULT_SEND_BUF_SIZE = 8192;
    private static final int DEFAULT_PORT = 19993;

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
            printStream.println(MqConfig.PRODUCER_ROLE_FLAG+","+config.getTopic());
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

    @Override
    public void run() {
        this.send();
    }
}
