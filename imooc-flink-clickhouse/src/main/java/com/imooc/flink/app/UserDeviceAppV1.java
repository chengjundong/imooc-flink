package com.imooc.flink.app;

import com.imooc.flink.domain.Access;
import com.imooc.flink.exception.JaredFlinkException;
import com.imooc.flink.json.JacksonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.typeutils.PojoTypeInfo;
import org.apache.flink.api.java.typeutils.ValueTypeInfo;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.guava18.com.google.common.hash.BloomFilter;
import org.apache.flink.shaded.guava18.com.google.common.hash.Funnels;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import javax.swing.*;
import java.util.*;

/**
 * Use deviceId and Bloom filter to check if user is a new user, write all new user data into Redis
 *
 * @author jared
 * @version 1
 * @since 2022/5/15
 */
public class UserDeviceAppV1 {

    private static final Logger LOG = LoggerFactory.getLogger(UserDeviceAppV1.class);
    private static final String TARGET_EVENT = "startup";

    private static final Jedis JEDIS = new Jedis("localhost", 6379);

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final Calendar now = new GregorianCalendar();
        final String hsetKey = new StringJoiner("-")
                .add("access")
                .add("newuser")
                .add("device")
                .add(String.valueOf(now.get(Calendar.YEAR)))
                .add(String.valueOf(now.get(Calendar.MONTH) + 1))
                .add(String.valueOf(now.get(Calendar.DAY_OF_MONTH)))
                .toString();

        // read source & convert to entity & filter error data & choose start-up event
        final SingleOutputStreamOperator<Access> dataAfterClean = env.readTextFile("data-file/access-v2.txt")
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
                .filter(a -> null != a && Objects.equals(TARGET_EVENT, a.getEvent()));


        dataAfterClean.keyBy(new KeySelector<Access, String>() {
            // key by OS
            @Override
            public String getKey(Access value) throws Exception {
                return value.getOs();
            }
        }).process(new KeyedProcessFunction<String, Access, Access>() {

            private transient ValueState<BloomFilter<String>> state;

            @Override
            public void open(Configuration parameters) throws Exception {
                state = this.getRuntimeContext().getState(
                        new ValueStateDescriptor<>("newUser", TypeInformation.of(new TypeHint<>() {
                        })));

                ValueState<ArrayList<Access>> ls = this.getRuntimeContext().getState(new ValueStateDescriptor<>("topN", TypeInformation.of(new TypeHint<>() {})));
            }

            @Override
            public void processElement(Access value, KeyedProcessFunction<String, Access, Access>.Context ctx, Collector<Access> out) throws Exception {
                String device = value.getDevice();

                BloomFilter<String> filter = state.value();
                if (null == filter) {
                    // init bloom filter
                    filter = BloomFilter.create(Funnels.unencodedCharsFunnel(), 10000);
                    state.update(filter);
                }

                if (!filter.mightContain(device)) {
                    // if it doesn't exist in bloom filter, then we collect it as result & put the deviceId in bloom-filter
                    out.collect(value);
                    filter.put(value.getDevice());
                }
            }
        }).addSink(new SinkFunction<>() {
            @Override
            public void invoke(Access value, Context context) throws Exception {
                try {
                    JEDIS.hset(hsetKey, value.getDevice(), value.getUid());
                } catch (Exception e) {
                    LOG.error("failed to write redis", e.getMessage());
                }
            }
        });

        env.execute("UserDeviceAppV1");
    }
}
