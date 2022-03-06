package com.imooc.flink.watermark;

import com.imooc.flink.socket.MySocketServer;
import com.imooc.flink.socket.TimeWordSocketDataGenerator;
import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.OutputTag;

import java.text.SimpleDateFormat;
import java.time.Duration;

import static org.apache.flink.util.Preconditions.checkArgument;
import static org.apache.flink.util.Preconditions.checkNotNull;

/**
 * Test how watermark generator works
 *
 * @author jared
 * @since 2022/2/20
 */
public class WatermarkGeneratorApp {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        final OutputTag<Tuple2<String, Integer>> late = new OutputTag<>("late-date") {
        };

        new Thread(new MySocketServer(new TimeWordSocketDataGenerator())).start();

        final SingleOutputStreamOperator<Tuple2<String, Integer>> ds = env.socketTextStream("127.0.0.1", 9090)
                .assignTimestampsAndWatermarks(
                        new WatermarkStrategy<String>() {
                            @Override
                            public WatermarkGenerator<String> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
                                return new MyBoundedOutOfOrdernessWatermarks<>(Duration.ofSeconds(1));
                            }
                        }.withTimestampAssigner(new TimestampAssignerSupplier<String>() {
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
//                .allowedLateness(Time.seconds(1000))
                .sideOutputLateData(late)
                .sum(1);

        // data flow
        ds.print();

        // side out put for late data
        ds.getSideOutput(late).printToErr();

        env.execute("WatermarkGeneratorApp");
    }

    /**
     * copy from {@link BoundedOutOfOrdernessWatermarks} and add print function
     *
     * @param <T>
     */
    private static class MyBoundedOutOfOrdernessWatermarks<T> implements WatermarkGenerator<T> {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        /**
         * The maximum timestamp encountered so far.
         */
        private long maxTimestamp;

        /**
         * The maximum out-of-orderness that this watermark generator assumes.
         */
        private final long outOfOrdernessMillis;

        public MyBoundedOutOfOrdernessWatermarks(Duration maxOutOfOrderness) {
            checkNotNull(maxOutOfOrderness, "maxOutOfOrderness");
            checkArgument(!maxOutOfOrderness.isNegative(), "maxOutOfOrderness cannot be negative");

            this.outOfOrdernessMillis = maxOutOfOrderness.toMillis();

            // start so that our lowest watermark would be Long.MIN_VALUE.
            this.maxTimestamp = Long.MIN_VALUE + outOfOrdernessMillis + 1;
        }

        @Override
        public void onEvent(T event, long eventTimestamp, WatermarkOutput output) {
//            System.out.println(">>>>>>>>>>>> onEvent triggered: " + dateFormat.format(eventTimestamp));
            maxTimestamp = Math.max(maxTimestamp, eventTimestamp);
        }

        @Override
        public void onPeriodicEmit(WatermarkOutput output) {
            final Watermark watermark = new Watermark(maxTimestamp - outOfOrdernessMillis - 1);
            output.emitWatermark(watermark);
//            System.out.println(">>>>>>>>>>>> onPeriodicEmit triggered: " + dateFormat.format(watermark.getTimestamp()));
        }
    }
}
