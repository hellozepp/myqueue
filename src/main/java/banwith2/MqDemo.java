package banwith2;

import java.io.*;

/**
 * @Author: zhanglin
 * @Date: 2019/3/29
 * @Time: 9:13 PM
 * * 数据报限流
 * * 创建队列，生产者生产数据放入队列，消费者消费数据读取队列。
 * * 并且对生产者进行限流，以获得1M/s的数据处理速率。java实现
 */
public class MqDemo {
    public static void main(String[] args) {
        MqConfig config = new MqConfig("topic01", "127.0.0.1", 30000);
        MqConfig config1 = new MqConfig("topic01", "127.0.0.1", 30003);
        FileReader fileReader = null;
        BufferedReader br = null;
        try {
            fileReader = new FileReader(new File("/Users/docker/Documents/testPack/NewFile.txt"));

            br = new BufferedReader(fileReader);
            MyProducerRunner myProducerRunner = new MyProducerRunner(config, br);
            MyConsumerRunner myConsumerRunner = new MyConsumerRunner(config1);
            Thread thread = new Thread(myProducerRunner);
            Thread thread1 = new Thread(myConsumerRunner);
            thread.start();
            thread1.start();

            try {
                thread.join();
                thread1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println("file path error!");
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (IOException e) {
                System.out.println("close IO exception!");
                e.printStackTrace();
            }
        }

    }
}
