package com.imooc.flink.watermark;

import com.imooc.flink.socket.MySocketServer;
import com.imooc.flink.socket.NumberSocketDataGenerator;
import com.imooc.flink.socket.TimeWordSocketDataGenerator;
import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.text.SimpleDateFormat;
import java.time.Duration;

/**
 * event time + watermark
 *
 * @author jared
 * @since 2022/2/20
 */
public class EventTimeWatermarkApp {

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
                .window(TumblingEventTimeWindows.of(Time.seconds(3)))
                .process(new ProcessWindowFunction<Tuple2<String, Integer>, Tuple2<String, Integer>, String, TimeWindow>() {

                    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

                    @Override
                    public void process(String s,
                                        ProcessWindowFunction<Tuple2<String, Integer>, Tuple2<String, Integer>, String, TimeWindow>.Context context,
                                        Iterable<Tuple2<String, Integer>> elements,
                                        Collector<Tuple2<String, Integer>> out) throws Exception {
                        System.out.println(">>>>>>>>>> window start: " + dateFormat.format(context.window().getStart()));
                        System.out.println(">>>>>>>>>> window end: " + dateFormat.format(context.window().getEnd()));
                        System.out.println(">>>>>>>>>> watermark: " + dateFormat.format(context.currentWatermark()));
                        Tuple2<String, Integer> result = null;
                        for (Tuple2<String, Integer> t2 : elements) {
                            if(result == null) {
                                result = t2;
                            } else {
                                result.f1 = result.f1+1;
                            }
                        }
                        out.collect(result);
                    }
                })
                .print();

        env.execute("EventTimeWatermarkApp");
    }
}
