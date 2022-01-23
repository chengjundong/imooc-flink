package com.imooc.flink.window;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import scala.Int;

/**
 * @author jucheng
 */
public class WindowAppTest {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        nonKeyWindowWithDeprecatedFunction(env);

        env.execute("WindowAppTest");
    }

    /**
     * Using deprecated function to process a non-key window
     *
     * @param env execution env
     */
    private static void nonKeyWindowWithDeprecatedFunction(StreamExecutionEnvironment env) {
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        env.socketTextStream("127.0.0.1", 9090)
                .map(new MapFunction<String, Long>() {

                    @Override
                    public Long map(String s) throws Exception {
                        return Long.valueOf(s);
                    }
                })
                .timeWindowAll(Time.seconds(1l))
                .sum(0)
                .print();
    }
}
