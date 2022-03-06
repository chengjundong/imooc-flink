package com.imooc.flink.redis;

/**
 * Parent interface for all redis hash-set data
 *
 * @author jared
 * @since 2022/1/3
 */
public interface IRedisHSetData {

    String getField();
    String getValue();
}
