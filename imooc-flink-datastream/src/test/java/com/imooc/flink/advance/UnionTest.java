package com.imooc.flink.advance;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author jared
 * @since 2021/12/11
 */
public class UnionTest {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> ds1 = env.readTextFile("data-file/batch-word-count.txt");
        DataStreamSource<String> ds2 = env.readTextFile("data-file/batch-word-count2.txt");
        ds1.union(ds2).print();

        env.execute("union job");
    }
}
