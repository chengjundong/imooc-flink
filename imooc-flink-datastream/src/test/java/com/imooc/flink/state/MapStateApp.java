package com.imooc.flink.state;

import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.client.program.StreamContextEnvironment;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Use MapState to calculate average
 * <pre>
 *     three PS5 video games: 1) Horizon Forbidden West 2) Elden Ring 3) GT7
 *     they have dynamic prices
 *     to calculate their average price all the time
 * </pre>
 *
 * @author jared
 * @since 2022/3/6
 */
public class MapStateApp {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamContextEnvironment.getExecutionEnvironment();

        env.fromCollection(dataSource())
                .keyBy(t2 -> t2.f0)
                .flatMap(new MapAvgFunction())
                .print();

        env.execute("MapStateApp");
    }

    private static List<Tuple2<String, Double>> dataSource() {
        final List<Tuple2<String, Double>> result = new ArrayList<>();
        String[] games = new String[]{"Horizon Forbidden West", "Elden Ring", "GT7"};

        for (int i = 0; i < 100; i++) {
            int index = ThreadLocalRandom.current().nextInt(0, 3);
            double amount = ThreadLocalRandom.current().nextDouble(100.00, 500.00);
            result.add(Tuple2.of(games[index], amount));
        }

        return result;
    }

    private static class MapAvgFunction extends RichFlatMapFunction<Tuple2<String, Double>, Tuple2<String, Double>> {

        private transient MapState<String, List<Double>> state;

        @Override
        public void open(Configuration parameters) throws Exception {
            MapStateDescriptor desc = new MapStateDescriptor<String, List<Double>>("avg",
                    TypeInformation.of(new TypeHint<>() {
                    }),
                    TypeInformation.of(new TypeHint<>() {
                    }));
            this.state = getRuntimeContext().getMapState(desc);
        }

        @Override
        public void flatMap(Tuple2<String, Double> input, Collector<Tuple2<String, Double>> out) throws Exception {
            List<Double> amounts = this.state.get(input.f0);

            // if the amount of game doesn't exist, create a new collection and put it in
            if (CollectionUtils.isEmpty(amounts)) {
                amounts = new ArrayList<>();
                amounts.add(input.f1);
                this.state.put(input.f0, amounts);

                // the 1st element is current average amount
                out.collect(input);
            } else {
                // add input amount to collection
                amounts.add(input.f1);
                // calculate average
                double sum = 0;
                for (Double amount : amounts) {
                    sum += amount;
                }

                double avg = BigDecimal.valueOf(sum / amounts.size()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                // collect average
                out.collect(Tuple2.of(input.f0, avg));
            }
        }
    }
}
