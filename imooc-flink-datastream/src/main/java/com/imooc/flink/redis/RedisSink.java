package com.imooc.flink.redis;

import org.apache.flink.api.common.functions.AbstractRichFunction;
import org.apache.flink.api.java.tuple.Tuple2;
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
 * @author jared
 * @since 2021/12/25
 * @see <a href="https://cloud.tencent.com/developer/article/1379438">Win10 + Redis + DockerDesktop</a>
 */
public class RedisSink extends AbstractRichFunction implements SinkFunction<Tuple2<String, Integer>> {

    private Jedis jedis;

    @Override
    public void open(Configuration parameters) throws Exception {
        this.jedis = new Jedis("localhost", 6379);
    }

    @Override
    public void close() throws Exception {
        this.jedis.close();
    }

    @Override
    public void invoke(Tuple2<String, Integer> value, Context context) throws Exception {
        this.jedis.hset("word-count", value.f0, value.f1.toString());
    }
}
