package com.imooc.flink.partitioner;

import com.imooc.flink.source.WordCountSource;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author jared
 * @since 2021/12/13
 */
public class WordCountPartitionerTest {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(6);

        env.addSource(new WordCountSource())
                .partitionCustom(new WordCountPartitioner(), s -> s)
                .map(new MapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> map(String value) throws Exception {
                        System.out.println("current thread id: " + Thread.currentThread().getId() + ", value: " + value);
                        return Tuple2.of(value, 1);
                    }
                }).print();

        env.execute("WordCountPartitionerTest");
    }
}
