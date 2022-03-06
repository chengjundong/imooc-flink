package com.imooc.flink.kafka;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author jared
 * @since 2022/1/15
 */
public class KafkaProducerTest {

    public static void main(String[] args) throws Exception {
        String broker = "192.168.10.106:61861";
        String topicName = "jared-test-20220115-6";
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        System.out.println("topic name: " + topicName);

//        try(Admin admin = Admin.create(properties)) {
//            int partitions = 1;
//            short replicationFactor = 1;
//            NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
//
//            CreateTopicsResult result = admin.createTopics(
//                    Collections.singleton(newTopic)
//            );
//
//            KafkaFuture<Void> future = result.values().get(topicName);
//            future.get();
//        }

        // produce
        for (;;) {
            Properties pp = new Properties();
            pp.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
            pp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            pp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            final KafkaProducer<String, String> producer = new KafkaProducer<>(pp, new StringSerializer(), new StringSerializer());
            producer.send(new ProducerRecord<>(topicName, 0, "a", "hello"));
        }
    }
}
