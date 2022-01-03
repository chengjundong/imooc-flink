package com.imooc.flink.redis;

import org.apache.flink.api.common.functions.AbstractRichFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import redis.clients.jedis.Jedis;

/**
 * <pre>
 *     # start redis in local
 *     docker run -d --name myredis -p6379:6379 -v /c/jared/redis-data:/data redis --appendonly yes
 *     # connect redis
 *     localhost:6379
 * </pre>
 *
 * @author jucheng
 * @see <a href="https://cloud.tencent.com/developer/article/1379438">Win10 + Redis + DockerDesktop</a>
 * @since 2021/12/25
 */
public class RedisT3Sink extends AbstractRichFunction implements SinkFunction<Tuple3<String, Integer, Long>> {

    private Jedis jedis;
    private final String hsetKey;

    public RedisT3Sink(String hsetKey) {
        this.hsetKey = hsetKey;
    }

    @Override
    public void open(Configuration parameters) {
        this.jedis = new Jedis("localhost", 6379);
    }

    @Override
    public void close() {
        this.jedis.close();
    }

    @Override
    public void invoke(Tuple3<String, Integer, Long> t3, Context context) {
        String key = t3.f0 + "_" + t3.f1;
        this.jedis.hset(hsetKey, key, t3.f2.toString());
    }
}
