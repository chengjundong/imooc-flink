package com.imooc.flink.window;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author jared
 */
public class SocketTest {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.socketTextStream("localhost", 9090).print();

        env.execute("SocketTest");
    }
}
