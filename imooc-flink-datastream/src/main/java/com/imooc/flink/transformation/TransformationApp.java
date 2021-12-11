package com.imooc.flink.transformation;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import scala.Tuple2;

import java.util.Objects;

public class TransformationApp {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        reduce(env);

        env.execute("tranformatin");
    }

    /**
     * Map one line of access.log to {@link Access}
     *
     * @param env
     * @throws Exception
     */
    public static void map(StreamExecutionEnvironment env) throws Exception {
        DataStreamSource<String> ds = env.readTextFile("data-file/access.log");

        ds.map(i -> {
            String[] splits = i.split(",");
            Long time = Long.valueOf(splits[0].trim());
            String domain = splits[1].trim();
            Double traffic = Double.valueOf(splits[2].trim());
            return new Access(time, domain, traffic);
        }).print();
    }

    /**
     * Filter by domain name, only keep "ebay.com"
     *
     * @param env
     * @throws Exception
     */
    public static void filter(StreamExecutionEnvironment env) throws Exception {
        DataStreamSource<String> ds = env.readTextFile("data-file/access.log");

        ds.map(i -> {
                    String[] splits = i.split(",");
                    Long time = Long.valueOf(splits[0].trim());
                    String domain = splits[1].trim();
                    Double traffic = Double.valueOf(splits[2].trim());
                    return new Access(time, domain, traffic);
                }).filter(a -> Objects.equals("ebay.com", a.getDomain()))
                .print();
    }

    /**
     * one -> many mapping
     *
     * @param env
     * @throws Exception
     */
    public static void flatMap(StreamExecutionEnvironment env) throws Exception {
        DataStreamSource<String> ds = env.readTextFile("data-file/access.log");

        ds.flatMap(new FlatMapFunction<String, String>() {
                    @Override
                    public void flatMap(String value, Collector<String> out) throws Exception {
                        for (String s1 : value.split(",")) {
                            out.collect(s1);
                        }
                    }
                }).filter(i -> i.contains(".com"))
                .print();
    }

    /**
     * similar to group-by, based on HASH value
     *
     * @param env
     * @throws Exception
     */
    public static void keyBy(StreamExecutionEnvironment env) throws Exception {
        DataStreamSource<String> ds = env.readTextFile("data-file/access.log");

        ds.map(i -> {
                    String[] raw = i.split(",");
                    DataObject result = new DataObject();
                    result.date = raw[0];
                    result.domain = raw[1];
                    result.count = Long.valueOf(raw[2]);
                    return result;
                })
                .keyBy(data -> data.domain)
                .sum("count")
                .print();
    }

    /**
     * word count
     *
     * @param env
     * @throws Exception
     */
    public static void reduce(StreamExecutionEnvironment env) throws Exception {
        DataStreamSource<String> ds = env.readTextFile("data-file/batch-word-count.txt");

        ds.flatMap(new FlatMapFunction<String, String>() {
                    @Override
                    public void flatMap(String value, Collector<String> out) throws Exception {
                        for (String s : value.split(",")) {
                            out.collect(s);
                        }
                    }
                }).map(new MapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> map(String value) throws Exception {
                        return Tuple2.apply(value, 1);
                    }
                }).keyBy(t -> t._1)
                .reduce(new ReduceFunction<Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> reduce(Tuple2<String, Integer> value1, Tuple2<String, Integer> value2) throws Exception {
                        return new Tuple2<>(value1._1, value1._2 + value2._2);
                    }
                });
    }

    public static final class DataObject {
        public String date;
        public String domain;
        public Long count;

        @Override
        public String toString() {
            return "DataObject{" +
                    "date='" + date + '\'' +
                    ", domain='" + domain + '\'' +
                    ", count=" + count +
                    '}';
        }
    }
}
