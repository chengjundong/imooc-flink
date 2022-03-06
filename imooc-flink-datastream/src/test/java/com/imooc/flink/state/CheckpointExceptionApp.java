package com.imooc.flink.state;

import com.imooc.flink.socket.MySocketServer;
import com.imooc.flink.socket.VideoGameSocketDataGenerator;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * To test how Flink can leverage checkpoint to recover job from exception
 *
 * @author jared
 * @since 2022/3/6
 */
public class CheckpointExceptionApp {

    public static void main(String[] args) throws Exception {
        // start socket server
        new Thread(new MySocketServer(new VideoGameSocketDataGenerator())).start();

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // set parallelism to 1, so you only can see one data flow error. Otherwise, you will set many data flow error, count is equal to parallelism
        // Source: Socket Stream -> Flat Map -> Map -> Sink: Print to Std. Out (1/1)#0 (08081efb3b8ea2691e20ebeb452ab2ab) switched from RUNNING to FAILED
        env.setParallelism(1);

        // 1s, one checkpoint
        env.enableCheckpointing(1000);
        // once the prev checkpoint is completed, trigger checkpoint after 1s
        env.getCheckpointConfig().setCheckpointInterval(1000);
        // restart 100 times, interval is 1s
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(100, Time.seconds(1)));
        // setup in-memory HMap state backend
//        env.setStateBackend(new HashMapStateBackend());

        // RocksDB state backend
//        env.setStateBackend(new EmbeddedRocksDBStateBackend());

        // local File state backend. I have to say, it is a real CPU killer. I even can't play video course when this app is running
        env.setStateBackend(new FsStateBackend("file:///C:/jared/IdeaProjects/imooc-flink/checkpoints"));
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        env.socketTextStream("localhost", 9090)
                .flatMap(new FlatMapFunction<String, String>() {

                    @Override
                    public void flatMap(String value, Collector<String> out) throws Exception {
                        for (String str : value.split(",")) {
                            out.collect(str);
                        }
                    }
                })
                .map(new MapFunction<String, Tuple2<String, Integer>>() {

                    @Override
                    public Tuple2<String, Integer> map(String value) throws Exception {
//                        if("FIFA2022".equals(value)) {
//                            throw new IllegalArgumentException("I don't play FIFA, I have a loyalty to PES!!");
//                        } else {
//                            return Tuple2.of(value, 1);
//                        }
                        return Tuple2.of(value, 1);
                    }
                })
                .keyBy(t2 -> t2.f0)
                .sum(1)
                .print();

        env.execute("CheckpointExceptionApp");
    }
}
