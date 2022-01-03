package com.imooc.flink.redis;

import org.apache.flink.api.java.tuple.Tuple2;

/**
 * Mapping Tuple 2 to Redis hash-set data
 *
 * @author jucheng
 * @since 2022/1/3
 */
public class Tuple2RedisHSetData<F1, F2> implements IRedisHSetData{

    private final Tuple2<F1, F2> t2;

    public Tuple2RedisHSetData(Tuple2<F1, F2> t2) {
        this.t2 = t2;
    }

    @Override
    public String getField() {
        return t2.f0.toString();
    }

    @Override
    public String getValue() {
        return t2.f1.toString();
    }
}
