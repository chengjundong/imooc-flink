package com.imooc.flink.app;

import com.imooc.flink.domain.Access;
import com.imooc.flink.exception.JaredFlinkException;
import com.imooc.flink.json.JacksonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.guava18.com.google.common.collect.Lists;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 需求
 * 1. 从access-v2.txt接受数据
 * 2. 按照event type、category、production name分组
 * 3. 获取每组的top 3
 *
 * @author jucheng
 * @since 4/10/2022
 */
public class TopNApp1 {

    private static final Logger LOG = LoggerFactory.getLogger(TopNApp1.class);

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.readTextFile("data-file/access-v2.txt")
                // json string -> Access entity
                .map(value -> {
                    if (StringUtils.isNotBlank(value)) {
                        try {
                            return JacksonUtils.fromJson(value, Access.class);
                        } catch (JaredFlinkException e) {
                            LOG.error(e.getMessage(), e);
                            return null;
                        }
                    } else {
                        LOG.error("input access data is blank!");
                        return null;
                    }
                })
                // filter out null & startup event & product can not be null
                .filter(a -> null != a && !"startup".equals(a.getEvent()) && null != a.getProduct())
                // assign watermark, using bounded out-of-orderness, extracting event time from access.time
                .assignTimestampsAndWatermarks(WatermarkStrategy
                        .<Access>forBoundedOutOfOrderness(Duration.ofSeconds(20))
                        .withTimestampAssigner((event, timestamp) -> event.getTime())
                )
                // key by event + category + product
                .keyBy(new KeySelector<Access, Tuple3<String, String, String>>() {
                    @Override
                    public Tuple3<String, String, String> getKey(Access value) throws Exception {
                        return Tuple3.of(value.getEvent(), value.getProduct().getCategory(), value.getProduct().getName());
                    }
                })
                // sliding window, window size 5 mins, sliding step 30s
                .window(SlidingEventTimeWindows.of(Time.minutes(5), Time.seconds(30)))
                // aggregate & collect the result, contains eventType / category / product name / count / window start / window end
                .aggregate(new EventCategoryProductAggregateFunction(), new ProcessWindowFunction<Long, EventCategoryProduct, Tuple3<String, String, String>, TimeWindow>() {

                    @Override
                    public void process(Tuple3<String, String, String> key,
                                        ProcessWindowFunction<Long, EventCategoryProduct, Tuple3<String, String, String>, TimeWindow>.Context context,
                                        Iterable<Long> elements,
                                        Collector<EventCategoryProduct> out) throws Exception {
                        EventCategoryProduct result = new EventCategoryProduct();
                        result.setEventType(key.f0);
                        result.setCategory(key.f1);
                        result.setProduct(key.f2);
                        result.setCount(elements.iterator().next());
                        result.setWindowStart(context.window().getStart());
                        result.setWindowEnd(context.window().getEnd());

                        out.collect(result);
                    }
                })
                // key by event type / category / window start / window end
                .keyBy(new KeySelector<EventCategoryProduct, Tuple4<String, String, Long, Long>>() {
                    /**
                     * @param value input 1st round of agg result
                     * @return key
                     * @throws Exception
                     * @implNote why window-start & window-end? the requirement asks us to agg per window, we need compare all agg result within one window
                     * so, we involve the window-start & window-end in key to group the agg result
                     */
                    @Override
                    public Tuple4<String, String, Long, Long> getKey(EventCategoryProduct value) throws Exception {
                        return Tuple4.of(value.getEventType(), value.getCategory(), value.getWindowStart(), value.getWindowEnd());
                    }
                })
                // sort & choose top 3
                .process(new KeyedProcessFunction<Tuple4<String, String, Long, Long>, EventCategoryProduct, List<EventCategoryProduct>>() {

                    private transient ListState<EventCategoryProduct> state;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        ListStateDescriptor<EventCategoryProduct> desc = new ListStateDescriptor<>("top3-state", EventCategoryProduct.class);
                        this.state = this.getRuntimeContext().getListState(desc);
                    }

                    @Override
                    public void processElement(EventCategoryProduct value,
                                               KeyedProcessFunction<Tuple4<String, String, Long, Long>, EventCategoryProduct, List<EventCategoryProduct>>.Context ctx,
                                               Collector<List<EventCategoryProduct>> out) throws Exception {
                        this.state.add(value);

                        // setup a timer, 1 ms after window end
                        ctx.timerService().registerEventTimeTimer(value.getWindowEnd() + 1);
                    }

                    @Override
                    public void onTimer(long timestamp,
                                        KeyedProcessFunction<Tuple4<String, String, Long, Long>, EventCategoryProduct, List<EventCategoryProduct>>.OnTimerContext ctx,
                                        Collector<List<EventCategoryProduct>> out) throws Exception {
                        // timer trigger sort
                        ArrayList<EventCategoryProduct> items = Lists.newArrayList(this.state.get());
                        // descend
                        items.sort((x, y) -> Long.compare(y.getCount(), x.getCount()));
                        // choose top 3
                        int size = Math.min(3, items.size());
                        ArrayList<EventCategoryProduct> result = new ArrayList<>();
                        for (int i = 0; i < size; i++) {
                            result.add(items.get(i));
                        }
                        // output
                        out.collect(result);
                    }
                })
                .print().setParallelism(1);

        env.execute("topN_1");
    }

    /**
     * accumulate each input tuple3 and get count
     */
    static class EventCategoryProductAggregateFunction implements AggregateFunction<Access, Long, Long> {

        @Override
        public Long createAccumulator() {
            return 0L;
        }

        @Override
        public Long add(Access value, Long accumulator) {
            return accumulator + 1;
        }

        @Override
        public Long getResult(Long accumulator) {
            return accumulator;
        }

        @Override
        public Long merge(Long a, Long b) {
            return a + b;
        }
    }

    static class EventCategoryProduct {
        private String eventType;
        private String category;
        private String product;
        private long count;
        private long windowStart;
        private long windowEnd;

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public long getWindowStart() {
            return windowStart;
        }

        public void setWindowStart(long windowStart) {
            this.windowStart = windowStart;
        }

        public long getWindowEnd() {
            return windowEnd;
        }

        public void setWindowEnd(long windowEnd) {
            this.windowEnd = windowEnd;
        }

        @Override
        public String toString() {
            return JacksonUtils.toJson(this);
        }
    }
}
