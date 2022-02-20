package com.imooc.flink.watermark;

import com.imooc.flink.socket.MySocketServer;
import com.imooc.flink.socket.TimeWordSocketDataGenerator;
import org.apache.flink.api.common.eventtime.TimestampAssigner;
import org.apache.flink.api.common.eventtime.TimestampAssignerSupplier;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;

/**
 * @author jucheng
 * @since 2022/2/20
 */
public class SessionWindowWatermarkApp {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        new Thread(new MySocketServer(new TimeWordSocketDataGenerator())).start();

        env.socketTextStream("127.0.0.1", 9090)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<String>forBoundedOutOfOrderness(Duration.ofSeconds(1))
                                .withTimestampAssigner(new TimestampAssignerSupplier<String>() {
                                    @Override
                                    public TimestampAssigner<String> createTimestampAssigner(Context context) {
                                        return new TimestampAssigner<String>() {
                                            @Override
                                            public long extractTimestamp(String element, long recordTimestamp) {
                                                return Long.parseLong(element.split(",")[0]);
                                            }
                                        };
                                    }
                                })
                )
                .map(new MapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> map(String s) throws Exception {
                        return Tuple2.of(s.split(",")[1], 1);
                    }
                })
                .keyBy(t2 -> t2.f0)
                // source data frequency is between 500ms and 3000ms
                .window(EventTimeSessionWindows.withGap(Time.milliseconds(1000)))
                .sum(1)
                .print();

        env.execute("SessionWindowWatermarkApp");
    }
}
