package com.imooc.flink.redis;

import org.apache.flink.api.common.functions.AbstractRichFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import redis.clients.jedis.Jedis;

import java.util.StringJoiner;

/**
 * <pre>
 *     # Sink any {@link Tuple} data into Redis hash set, the arity of tuple is N
 *     - key -> join Tuple.f(0) ~ Tuple.f(N-1) by joiner "_"
 *     - value -> Tuple.f(N)
 *     # start redis in local
 *     docker run -d --name myredis -p6379:6379 -v /c/jared/redis-data:/data redis --appendonly yes
 *     # connect redis
 *     localhost:6379
 * </pre>
 *
 * @author jared
 * @see <a href="https://cloud.tencent.com/developer/article/1379438">Win10 + Redis + DockerDesktop</a>
 * @since 2021/12/25
 */
public class RedisTupleSink<T extends Tuple> extends AbstractRichFunction implements SinkFunction<T> {

    private Jedis jedis;
    private final String hsetKey;

    public RedisTupleSink(String hsetKey) {
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
    public void invoke(T t, Context context) {
        final int count = t.getArity();
        final StringJoiner sj = new StringJoiner("_");
        for (int i = 0; i < count - 1; i++) {
            sj.add(t.getField(i).toString());
        }
        this.jedis.hset(hsetKey, sj.toString(), t.getField(count-1).toString());
    }
}
