package com.imooc.flink.window;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.flink.pojo.UserAccountBalance;
import com.imooc.flink.socket.*;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.*;

/**
 * @author jucheng
 */
public class WindowAppTest {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

//        nonKeyWindowWithDeprecatedFunction(env);
        keyedWindow_Agg(env);
//        nonKeyWindow_SessionWindow(env);
//        keyedWindow_UserAccountBalance(env);
//        keyedWindow_Lowest3_UserAccountBalance(env);
//        keyedWindow_Lowest3_SumUserAccountBalance(env);

        env.execute("WindowAppTest");
    }

    /**
     * Using deprecated function to process a non-key window
     *
     * @param env execution env
     */
    private static void nonKeyWindowWithDeprecatedFunction(StreamExecutionEnvironment env) {
        new Thread(new MySocketServer(new NumberSocketDataGenerator())).start();

        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        env.socketTextStream("127.0.0.1", 9090)
                .map(new MapFunction<String, Long>() {

                    @Override
                    public Long map(String s) throws Exception {
                        return Long.valueOf(s);
                    }
                })
                .timeWindowAll(Time.seconds(1l))
                .sum(0)
                .print();
    }

    /**
     * Keyed window + agg function: The most sold video games per platform
     * Frequency: In every 1s, account for the previous 5s selling result
     *
     * @param env execution env
     */
    private static void keyedWindow_Agg(StreamExecutionEnvironment env) {
        new Thread(new MySocketServer(new VideoGameSocketDataGenerator())).start();

        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        env.socketTextStream("127.0.0.1", 9090)
                .map(new MapFunction<String, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> map(String s) throws Exception {
                        final String[] ss = s.split(",");
                        return Tuple3.of(ss[0], ss[1], 1);
                    }
                })
                .keyBy(t3 -> t3.f0)
                .window(SlidingProcessingTimeWindows.of(Time.seconds(5), Time.seconds(1)))
                .aggregate(new AggregateFunction<Tuple3<String, String, Integer>, Map<String, Integer>, Tuple3<String, String, Integer>>() {

                    private String platform = "";

                    @Override
                    public Map<String, Integer> createAccumulator() {
                        return new HashMap<>();
                    }

                    @Override
                    public Map<String, Integer> add(Tuple3<String, String, Integer> value, Map<String, Integer> accumulator) {
                        if("".equals(platform)) {
                            platform = value.f0;
                        }
                        if(accumulator.containsKey(value.f1)) {
                            accumulator.put(value.f1, accumulator.get(value.f1) + value.f2);
                        } else {
                            accumulator.put(value.f1, value.f2);
                        }
                        return accumulator;
                    }

                    @Override
                    public Tuple3<String, String, Integer> getResult(Map<String, Integer> accumulator) {
                        String mostSoldName = "";
                        int mostSoldNum = Integer.MIN_VALUE;
                        for (Map.Entry<String, Integer> e : accumulator.entrySet()) {
                            if("".endsWith(mostSoldName)) {
                                mostSoldName = e.getKey();
                                mostSoldNum = e.getValue();
                            } else if(mostSoldNum < e.getValue()) {
                                mostSoldName = e.getKey();
                                mostSoldNum = e.getValue();
                            }
                        }
                        return Tuple3.of(platform, mostSoldName, mostSoldNum);
                    }

                    @Override
                    public Map<String, Integer> merge(Map<String, Integer> a, Map<String, Integer> b) {
                        // create a result based A
                        final HashMap<String, Integer> result = new HashMap<>(a);
                        // if B.key exists in result, then get value & sum them, finally, put the sum result back to result
                        // if B.key doesn't exist in result, then put B.key + B.value in result
                        for (Map.Entry<String, Integer> e : b.entrySet()) {
                            if(result.containsKey(e.getKey())) {
                                result.put(e.getKey(), result.get(e.getKey()) + e.getValue());
                            } else {
                                result.put(e.getKey(), e.getValue());
                            }
                        }
                        return result;
                    }
                })
                .print();
    }

    /**
     * To use keyed window to get each user's lowest balance in 5s
     *
     * @param env execution env
     */
    private static void keyedWindow_UserAccountBalance(StreamExecutionEnvironment env) {
        new Thread(new MySocketServer(new UserBalanceSocketDataGenerator())).start();
        final ObjectMapper jackson2 = new ObjectMapper();

        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        env.socketTextStream("127.0.0.1", 9090)
                // only care about Asset account
                .filter(s -> 1 == jackson2.readValue(s, UserAccountBalance.class).getType())
                .map(new MapFunction<String, Tuple3<Long, String, Integer>>() {

                    @Override
                    public Tuple3<Long, String, Integer> map(String value) throws Exception {
                        final UserAccountBalance accountBalance = jackson2.readValue(value, UserAccountBalance.class);
                        return Tuple3.of(accountBalance.getUserId(), accountBalance.getBalance(), accountBalance.getUnitId());
                    }
                })
                .keyBy(t3 -> t3.f0)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(1L)))
                .reduce(new ReduceFunction<Tuple3<Long, String, Integer>>() {
                    @Override
                    public Tuple3<Long, String, Integer> reduce(Tuple3<Long, String, Integer> t3_1, Tuple3<Long, String, Integer> t3_2) throws Exception {
                        String lowerBal = String.valueOf(Double.min(Double.parseDouble(t3_1.f1), Double.parseDouble(t3_2.f1)));
                        return Tuple3.of(t3_1.f0, lowerBal, t3_1.f2);
                    }
                })
                .print();
    }

    /**
     * To use keyed window to list the lowest three each user's balance in 5s
     *
     * @param env execution env
     */
    private static void keyedWindow_Lowest3_UserAccountBalance(StreamExecutionEnvironment env) {
        new Thread(new MySocketServer(new UserBalanceSocketDataGenerator())).start();
        final ObjectMapper jackson2 = new ObjectMapper();

        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        env.socketTextStream("127.0.0.1", 9090)
                // only care about Asset account
                .filter(s -> 1 == jackson2.readValue(s, UserAccountBalance.class).getType())
                .map(new MapFunction<String, UserAccountBalance>() {

                    @Override
                    public UserAccountBalance map(String value) throws Exception {
                        return jackson2.readValue(value, UserAccountBalance.class);
                    }
                })
                .keyBy(UserAccountBalance::getUnitId)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(1L)))
                .process(new Lowest3ProcessingWindow())
                .print();
    }

    private static class Lowest3ProcessingWindow extends ProcessWindowFunction<UserAccountBalance, UserAccountBalance, Integer, TimeWindow> {

        @Override
        public void process(Integer key,
                            Context context,
                            Iterable<UserAccountBalance> elements,
                            Collector<UserAccountBalance> out) throws Exception {
            System.out.println(">>>>>>>>>>>>>>> process function#" + Thread.currentThread().getId());
            // find the lowest balance per each user in same unitId
            Map<Long, UserAccountBalance> lowestBalancePerUser = new HashMap<>();
            elements.forEach(bal -> {
                if(lowestBalancePerUser.containsKey(bal.getUserId())) {
                    final UserAccountBalance existing = lowestBalancePerUser.get(bal.getUserId());
                    // if existing < input balance, then choose existing
                    // else, choose input balance
                    UserAccountBalance lower = -1 == bal.getBalanceInBigDecimal().compareTo(existing.getBalanceInBigDecimal()) ? bal : existing;
                    lowestBalancePerUser.put(bal.getUserId(), lower);
                } else {
                    lowestBalancePerUser.put(bal.getUserId(), bal);
                }
            });
            // sort and find the lowest 3 user
            List<UserAccountBalance> lowest3 = new ArrayList<>(lowestBalancePerUser.values());
            Collections.sort(lowest3, new Comparator<UserAccountBalance>() {
                @Override
                public int compare(UserAccountBalance o1, UserAccountBalance o2) {
                    return o1.getBalanceInBigDecimal().subtract(o2.getBalanceInBigDecimal()).intValue();
                }
            });
            for (int i=0; i<3 && i<lowest3.size(); i++) {
                out.collect(lowest3.get(i));
            }
        }
    }

    /**
     * non-key window + session window, gap between two windows is 1s
     *
     * @param env execution env
     */
    private static void nonKeyWindow_SessionWindow(StreamExecutionEnvironment env) {
        new Thread(new MySocketServer(new NumberSocketDataGeneratorWithinSleep())).start();

        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        env.socketTextStream("127.0.0.1", 9090)
                .map(new MapFunction<String, Long>() {

                    @Override
                    public Long map(String s) throws Exception {
                        return Long.valueOf(s);
                    }
                })
                .windowAll(ProcessingTimeSessionWindows.withGap(Time.seconds(1)))
                .sum(0)
                .print();
    }
}
