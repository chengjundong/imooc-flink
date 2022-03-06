package com.imooc.flink.app;

import com.imooc.flink.domain.Access;
import com.imooc.flink.exception.JaredFlinkException;
import com.imooc.flink.json.JacksonUtils;
import com.imooc.flink.redis.RedisTupleSink;
import com.imooc.flink.udf.AsyncProvinceChannelFunction;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

/**
 * To analyze user count by province and channel
 *
 * @author jared
 * @since 2022/1/15
 */
public class UserProvinceChannelAppV1 {

    private static final Logger LOG = LoggerFactory.getLogger(UserProvinceChannelAppV1.class);
    private static final String TARGET_EVENT = "startup";

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final Calendar now = new GregorianCalendar();
        final String hsetKey = new StringJoiner("-")
                .add("access")
                .add("user")
                .add("province")
                .add("channel")
                .add(String.valueOf(now.get(Calendar.YEAR)))
                .add(String.valueOf(now.get(Calendar.MONTH) + 1))
                .add(String.valueOf(now.get(Calendar.DAY_OF_MONTH)))
                .toString();

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
                // access exist; start_up event; ip address exists
                .filter(a -> null != a && Objects.equals(TARGET_EVENT, a.getEvent()) && StringUtils.isNotBlank(a.getIp()));

        // map access data to T3 contains province in async + unorder mode
        AsyncDataStream.unorderedWait(dataAfterClean, new AsyncProvinceChannelFunction(), 5, TimeUnit.SECONDS, 10000)
                // only keep the data which has province
                .filter(t4 -> StringUtils.isNotBlank(t4.f0))
                .keyBy(new KeySelector<Tuple4<String, String, Integer, Long>, Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> getKey(Tuple4<String, String, Integer, Long> t4) throws Exception {
                        return Tuple2.of(t4.f0, t4.f2);
                    }
                })
                .sum(3)
                .addSink(new RedisTupleSink(hsetKey));

        env.execute("UserProvinceAppV1");
    }
}
