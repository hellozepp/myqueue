package limit.common;

/**
 * @Author: zhanglin
 * @Date: 2019/3/29
 * @Time: 9:13 PM
 */
public class MqConfig {
    private String topic;
    /** the param is ip:port */
    private String serverIp;
    private Integer serverPort;
    public static final Integer CONSUMER_ROLE_FLAG = 0;
    public static final Integer PRODUCER_ROLE_FLAG = 1;
    public static final String SYMBOL_BITS = ",";
    public static final Integer SYMBOL_SIZE = 2;

    public MqConfig(String topic, String serverIp, Integer serverPort) {
        this.topic = topic;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }
}
