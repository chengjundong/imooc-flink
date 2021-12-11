package com.imooc.flink;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * <h3>Using data-streaming to count the word from Socket</h3>
 * <pre>
 *     1. read data from socket
 *     2. split by comma
 *     3. count the word
 *     4. output to console
 * </pre>
 *
 * @author jucheng
 */
public class StreamingWordCountApp {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> stream = env.socketTextStream("localhost", 8080);

        stream
                .flatMap(new FlatMapFunction<String, String>() {
                    @Override
                    public void flatMap(String input, Collector<String> collector) throws Exception {
                        String[] words = input.split(",");
                        for (String word : words) {
                            collector.collect(word);
                        }
                    }
                })
                .filter(word -> StringUtils.isNotBlank(word))
                .map(new MapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> map(String s) throws Exception {
                        return new Tuple2<>(s, 1);
                    }
                })
                .keyBy(t2 -> t2.f0)
                .sum(1)
                .print();

        env.execute("Word Count");
    }
}
