package com.imooc.flink.rich;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;

/**
 * @author jucheng
 * @since 2021/12/11
 */
public class RichFileMapFunction extends RichMapFunction<String, Tuple2<String, Integer>> {

    @Override
    public void open(Configuration parameters) throws Exception {
        RuntimeContext ctx = this.getRuntimeContext();
        System.out.println(">>>>>>>>>> open, job id: " + ctx.getJobId());
    }

    @Override
    public void close() throws Exception {
        System.out.println(">>>>>>>>>> close");
    }

    @Override
    public Tuple2<String, Integer> map(String value) throws Exception {
        return new Tuple2<>(value, 1);
    }
}
