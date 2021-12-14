package com.imooc.flink.rich;

import com.imooc.flink.jdbc.DatabaseWordCountSink;
import com.imooc.flink.jdbc.DatabaseWordCountSource;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author jucheng
 * @since 2021/12/11
 */
public class DatabaseConnectorAndSinkTest {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> ds = env.addSource(new DatabaseWordCountSource());

        ds.map(new RichFileMapFunction())
                .keyBy(t2 -> t2.f0)
                .sum(1)
                .addSink(new DatabaseWordCountSink());

        env.execute("DatabaseConnectorAndSinkTest ==> postgreSQL");
    }
}
