package qpslimit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
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
            socket.setSendBufferSize(1);
            socket.bind(new InetSocketAddress(9998));
            socket.connect(new InetSocketAddress("localhost", 30001));//连接远程服务端接口
            PrintStream ps = new PrintStream(socket.getOutputStream());
            ps.println("谢谢祝福！谢谢祝福！谢谢祝福！谢谢祝福！谢谢祝福！谢谢祝福！谢谢祝福！谢谢祝福！谢谢祝福！谢谢祝福！谢谢祝福！谢谢祝福！");
            ps.flush();
            // 将Socket对应的输入流包装成BufferedReader
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            // 进行普通IO操作
            String line = br.readLine();
            System.out.println("来自服务器的数据：" + line);

            ps.close();
            // 关闭输入流、socket
            br.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
