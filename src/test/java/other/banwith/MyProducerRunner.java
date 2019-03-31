package other.banwith;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @Author: zhanglin
 * @Date: 2019/3/30
 * @Time: 12:49 AM
 */
public class MyProducerRunner implements Runnable {
    @Override
    public void run() {
        Socket socket;
        try {
            socket = new Socket();
            socket.setSendBufferSize(8192);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(9987));
            socket.connect(new InetSocketAddress("127.0.0.1", 30000));//连接远程服务端接口
            System.out.println("MyProducerRunner has started in the " + socket.getInetAddress().toString() + " :" + socket.getLocalPort() + "...");
            FileReader fileReader = new FileReader(new File("/Users/docker/Documents/testPack/submit.sh"));
            BufferedReader br = new BufferedReader(fileReader);
            PrintStream printStream = new PrintStream(socket.getOutputStream());
            String str;
            while ((str = br.readLine()) != null) {
                printStream.println(str);
            }
            printStream.flush();
            printStream.close();

            // 将Socket对应的输入流包装成BufferedReader
            BufferedReader brByServer = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            // 进行普通IO操作
            String line = brByServer.readLine();
            System.out.println("Msg from the server :" + line);
            // 关闭输入流、socket
            br.close();
            brByServer.close();
            fileReader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MyProducerRunner().run();
    }
}
