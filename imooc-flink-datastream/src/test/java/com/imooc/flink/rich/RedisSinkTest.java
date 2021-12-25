package com.imooc.flink.rich;

import com.imooc.flink.redis.RedisSink;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author jucheng
 * @since 2021/12/25
 */
public class RedisSinkTest {

    public static void main(String[] args) throws Exception {
        String filePath = "data-file/batch-word-count.txt";
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.readTextFile(filePath)
                .flatMap(new RichFileFlatMapFunction())
                .keyBy(t2 -> t2.f0)
                .sum(1)
                .addSink(new RedisSink());

        env.execute("RedisSinkTest");
    }
}
