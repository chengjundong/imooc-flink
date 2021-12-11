package com.imooc.flink.rich;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author jucheng
 * @since 2021/12/11
 */
public class RichFlatMapTest {

    public static void main(String[] args) throws Exception {
        String filePath = "data-file/batch-word-count.txt";
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(3);

        DataStreamSource<String> ds = env.readTextFile(filePath);
        System.out.println("ds parallelism: " + ds.getParallelism());

        SingleOutputStreamOperator<Tuple2<String, Integer>> fm = ds.flatMap(new RichFileFlatMapFunction());
        System.out.println("flatmap parallelism: " + fm.getParallelism());

        KeyedStream<Tuple2<String, Integer>, String> kb = fm.keyBy(t2 -> t2.f0);
        System.out.println("keyBy parallelism: " + kb.getParallelism());

        SingleOutputStreamOperator<Tuple2<String, Integer>> sum = kb.sum(1);
        System.out.println("sum parallelism: " + sum.getParallelism());

        sum.print();

        env.execute("wd in rich map");
    }
}
