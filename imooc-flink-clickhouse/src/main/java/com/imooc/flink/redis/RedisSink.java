package com.imooc.flink.redis;

import org.apache.flink.api.common.functions.AbstractRichFunction;
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
public class RedisSink extends AbstractRichFunction implements SinkFunction<IRedisHSetData> {

    private final Jedis jedis;
    private final String hsetKey;

    public RedisSink(Jedis jedis, String hsetKey) {
        this.jedis = jedis;
        this.hsetKey = hsetKey;
    }

    public RedisSink(String hsetKey) {
        this.jedis = new Jedis("localhost", 6379);
        this.hsetKey = hsetKey;
    }

    @Override
    public void open(Configuration parameters) {
    }

    @Override
    public void close() {
        this.jedis.close();
    }

    @Override
    public void invoke(IRedisHSetData data, Context context) {
        this.jedis.hset(hsetKey, data.getField(), data.getValue());
    }
}
