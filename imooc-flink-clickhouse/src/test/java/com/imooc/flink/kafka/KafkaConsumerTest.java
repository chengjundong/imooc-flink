package com.imooc.flink.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * @author jared
 * @since 2022/1/15
 */
public class KafkaConsumerTest {

    public static void main(String[] args) {
        String broker = "192.168.10.106:61861";
        String topicName = "jared-test-20220115-6";
        Properties cp = new Properties();
        cp.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        cp.put(ConsumerConfig.GROUP_ID_CONFIG, "0");
        cp.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        cp.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        cp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        cp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(cp, new StringDeserializer(), new StringDeserializer());
        consumer.subscribe(Collections.singleton(topicName));
        System.out.println("subscribe: " + consumer.subscription());
        for(;;) {
            final ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            records.forEach(r -> System.out.println(r.value()));
        }
    }
}
