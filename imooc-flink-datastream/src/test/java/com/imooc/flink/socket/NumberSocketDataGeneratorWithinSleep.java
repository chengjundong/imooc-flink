package com.imooc.flink.socket;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Sleep randomly from 800ms to 2000ms, it is used to simulate data gap
 *
 * @author jucheng
 * @since 2022/2/12
 */
public class NumberSocketDataGeneratorWithinSleep implements SocketDataGenerator{

    @Override
    public String generateData() {
        try {
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextLong(800, 2000));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(ThreadLocalRandom.current().nextInt(0, 1000));
    }
}
