package com.imooc.flink.domain;

/**
 * value object represents one access data
 *
 * <pre>
 *     {
 *          "deviceType": "iPhone 10",
 *          "uid": "user_100",
 *          "product": {
 *              "name": "SparkSQL极速入门整合Kudu实现广告业务数据分析",
 *              "category": "大数据实战"
 *          },
 *          "os": "iOS",
 *          "ip": "210.29.115.1",
 *          "nu": 1,
 *          "channel": "华为商城",
 *          "time": 1748389588524,
 *          "event": "browse",
 *          "net": "WiFi",
 *          "device": "840dad95-dc4b-45f8-8388-56bf91fbcde8",
 *          "version": "V1.2.0"
 *     }
 * </pre>
 *
 * @author jucheng
 * @since 2022/1/3
 */
public class Access {

    private String deviceType;
    private String uid;
    private Product product;
    /**
     * client device operating system
     */
    private String os;
    private String ip;
    /**
     * 1 -- new user ; not 1 -- old user
     */
    private int nu;
    private String channel;
    private long time;
    private String event;
    private String net;
    private String device;
    private String version;

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getNu() {
        return nu;
    }

    public void setNu(int nu) {
        this.nu = nu;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getNet() {
        return net;
    }

    public void setNet(String net) {
        this.net = net;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * refer to {@link org.apache.flink.api.common.functions.util.PrintSinkOutputWriter#write(Object)}, it uses toString() method to get text of entity
     *
     * @return text
     */
    @Override
    public String toString() {
        return "Access{" +
                "deviceType='" + deviceType + '\'' +
                ", uid='" + uid + '\'' +
                ", product=" + product +
                ", os='" + os + '\'' +
                ", ip='" + ip + '\'' +
                ", nu=" + nu +
                ", channel='" + channel + '\'' +
                ", time=" + time +
                ", event='" + event + '\'' +
                ", net='" + net + '\'' +
                ", device='" + device + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
