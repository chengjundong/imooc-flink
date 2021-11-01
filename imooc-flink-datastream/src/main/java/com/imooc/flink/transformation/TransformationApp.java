package com.imooc.flink.transformation;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Objects;

public class TransformationApp {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        filter(env);

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
}
