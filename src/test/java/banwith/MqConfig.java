package banwith;

import java.util.List;

/**
 * @Author: zhanglin
 * @Date: 2019/3/31
 * @Time: 12:36 AM
 */
public class MqConfig {
    private String topic;
    //the param is ip:port
    private List<String> producerList;
    private List<String> consumerList;

    public MqConfig(String topic, List<String> producerList, List<String> consumerList) {
        this.topic = topic;
        this.producerList = producerList;
        this.consumerList = consumerList;
    }
}
