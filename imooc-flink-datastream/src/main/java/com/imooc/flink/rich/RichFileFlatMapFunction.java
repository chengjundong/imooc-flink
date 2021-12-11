package com.imooc.flink.rich;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

/**
 * @author jucheng
 * @since 2021/12/11
 */
public class RichFileFlatMapFunction extends RichFlatMapFunction<String, Tuple2<String, Integer>> {

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
    public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
        for (String v : value.split(",")) {
            Tuple2<String, Integer> t2 = new Tuple2<>(v, 1);
            out.collect(t2);
        }
    }
}
