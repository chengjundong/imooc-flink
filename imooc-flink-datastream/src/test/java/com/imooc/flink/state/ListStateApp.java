package com.imooc.flink.state;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @author jared
 * @since 2022/3/6
 */
public class ListStateApp {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // expect: 1,5.5 / 1,6 / 2,5.5
        env.fromElements(Tuple2.of(1, 1), Tuple2.of(1, 10), Tuple2.of(1, 7), Tuple2.of(2, 6), Tuple2.of(2, 5))
                .keyBy(t2 -> t2.f0)
                .flatMap(new RichFlatMapFunction<Tuple2<Integer, Integer>, Tuple2<Integer, Double>>() {
                    private transient ListState<Integer> state;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        final ListStateDescriptor<Integer> desc = new ListStateDescriptor<>("avg", TypeInformation.of(new TypeHint<>() {
                        }));
                        this.state = this.getRuntimeContext().getListState(desc);
                    }

                    @Override
                    public void flatMap(Tuple2<Integer, Integer> input, Collector<Tuple2<Integer, Double>> out) throws Exception {
                        if(this.state.get().iterator().hasNext()) {
                            this.state.add(input.f1);
                            double sum = 0;
                            long count = 0;
                            for (Integer e : this.state.get()) {
                                sum += e;
                                count++;
                            }
                            double avg = sum/count;
                            out.collect(Tuple2.of(input.f0, avg));
                        } else {
                            // the 1st element
                            this.state.add(input.f1);
                        }
                    }
                }).print();

        env.execute("ListStateApp");
    }
}
