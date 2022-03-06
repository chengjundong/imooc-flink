package com.imooc.flink.app;

import com.imooc.flink.domain.Access;
import com.imooc.flink.exception.JaredFlinkException;
import com.imooc.flink.json.JacksonUtils;
import com.imooc.flink.redis.RedisT3Sink;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Analyze user behavior by operating system
 *
 * @author jared
 * @version 1
 * @since 2022/1/3
 */
public class UserOsAppV1 {

    private static final Logger LOG = LoggerFactory.getLogger(UserOsAppV1.class);
    private static final String TARGET_EVENT = "startup";

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final Calendar now = new GregorianCalendar();
        final String hsetKey = new StringJoiner("-")
                .add("access")
                .add("user")
                .add("os")
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
                .filter(a -> null != a && Objects.equals(TARGET_EVENT, a.getEvent()));

        dataAfterClean.map(new MapFunction<Access, Tuple3<String, Integer, Long>>() {
                    @Override
                    public Tuple3<String, Integer, Long> map(Access access) throws Exception {
                        return Tuple3.of(access.getOs(), access.getNu(), 1L);
                    }
                })
                .keyBy(new KeySelector<Tuple3<String, Integer, Long>, Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> getKey(Tuple3<String, Integer, Long> t3) throws Exception {
                        return Tuple2.of(t3.f0, t3.f1);
                    }
                })
                .sum(2)
                .addSink(new RedisT3Sink(hsetKey));

        // (iOS,0,16)
        // (Android,0,17)
        // (iOS,1,38)
        // (Android,1,29)

        env.execute("UserOsAppV1");
    }
}
