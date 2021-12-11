package com.imooc.flink.advance;

import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import scala.Int;

import java.util.ArrayList;

/**
 * @author jucheng
 * @since 2021/12/11
 */
public class ConnectTest {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        ArrayList<String> l1 = new ArrayList<>(){
            {
                this.add("true");
                this.add("false");
            }
        };
        DataStreamSource<String> ds1 = env.fromCollection(l1);

        ArrayList<Integer> l2 = new ArrayList<>(){
            {
                this.add(1);
                this.add(2);
            }
        };
        ConnectedStreams<Integer, String> ds12 = env.fromCollection(l2).connect(ds1);

        ds12.map(new CoMapFunction<Integer, String, Boolean>() {
            @Override
            public Boolean map1(Integer value) throws Exception {
                return value % 2 == 0;
            }

            @Override
            public Boolean map2(String value) throws Exception {
                return Boolean.valueOf(value);
            }
        }).print();

        env.execute("connect operator job");
    }
}
