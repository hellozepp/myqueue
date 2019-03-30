package rateLimiter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @Author: zhanglin
 * @Date: 2019/3/29
 * @Time: 9:13 PM
 */
public class MyMessageQueueServer {
    private LinkedBlockingDeque<byte[]> linkedBlockingDeque = new LinkedBlockingDeque<>();
    private static Selector selector;

    public MyMessageQueueServer() {
    }

    static {
        try {
            init();
        } catch (IOException e) {
            System.out.println("[MyMessageQueueServer] init the ServerSocketChannel error:" + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void init() throws IOException {
        int backlog = 50;//backlog是等待队列的大小
        ServerSocketChannel server = ServerSocketChannel.open();

        selector = Selector.open();
        server.socket().setReceiveBufferSize(1024);
        // 开启非阻塞
        server.configureBlocking(false);
        // 开启监听
        server.socket().bind(new InetSocketAddress(30000), backlog);
        server.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void testReceiver() throws Exception {
        while (true) {
            selector.select();// 当前操作会阻塞，直到至少一个通道被选择
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            SelectionKey selectionKey = null;
            while (iterator.hasNext()) {
                selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    ServerSocketChannel ssc = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel accept = ssc.accept();
                    // 设置阻塞
                    accept.configureBlocking(false);
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    System.out.println(accept.getRemoteAddress() + "客户端连接");
                    accept.register(selectionKey.selector(), SelectionKey.OP_READ, byteBuffer);
                } else if (selectionKey.isReadable()) {
                    SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
                    buffer.clear();
                    while (clientChannel.read(buffer) > 0) {
                        buffer.flip();
                        byte[] as = new byte[buffer.limit()];
                        buffer.get(as);
                        linkedBlockingDeque.add(as);
                        System.out.println(Thread.currentThread().getName() + "接收的数据：" + new String(as));
                        buffer.clear();
                    }
                }
                iterator.remove();
            }
        }
    }

    public void testSender() throws Exception {

    }
}
