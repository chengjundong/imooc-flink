package com.imooc.flink.socket;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author jucheng
 * @since 2022/2/12
 */
public class NumberSocketDataGenerator implements SocketDataGenerator{

    @Override
    public String generateData() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(0, 1000));
    }
}
