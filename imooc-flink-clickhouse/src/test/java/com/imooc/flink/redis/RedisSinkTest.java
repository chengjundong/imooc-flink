package com.imooc.flink.redis;

import org.apache.flink.api.java.tuple.Tuple2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import static org.mockito.BDDMockito.*;

/**
 * @author jucheng
 * @since 2022/1/3
 */
public class RedisSinkTest {

    private RedisSink sink;
    private Jedis jedis;
    private final String hsetKey = "hello-redis";

    @BeforeEach
    private void setUp() {
        jedis = mock(Jedis.class);
        sink = new RedisSink(jedis, hsetKey);
    }

    @Test
    public void invoke_InputTuple2_Put() {
        final Tuple2<String, Integer> t2 = Tuple2.of("abc", 12);
        final Tuple2RedisHSetData<String, Integer> data = new Tuple2RedisHSetData<>(t2);

        sink.invoke(data, null);

        then(jedis).should().hset(hsetKey, t2.f0, t2.f1.toString());
    }
}
