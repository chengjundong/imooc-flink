package com.imooc.flink.kafka;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

public class KafkaConnectorTest {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "192.168.1.18:32768");
        DataStream<String> stream = env.addSource(new FlinkKafkaConsumer<>("t-2021101200", new SimpleStringSchema(), properties));

        stream.print();

        env.execute("my flink test");
    }
}
