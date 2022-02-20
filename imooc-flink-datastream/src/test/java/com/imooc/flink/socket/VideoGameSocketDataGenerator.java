package com.imooc.flink.socket;

import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author jucheng
 * @since 2022/2/20
 */
public class VideoGameSocketDataGenerator implements SocketDataGenerator {

    private final String[] games = new String[]{"FIFA2022", "Witcher 3", "Assassin's Creed"};
    private final String[] platforms = new String[]{"PS5", "Nintendo Switch", "PC"};

    /**
     * @return {platform},{game}
     * @throws Exception
     */
    @Override
    public String generateData() throws Exception {
        final int index1 = ThreadLocalRandom.current().nextInt(0, 3);
        final int index2 = ThreadLocalRandom.current().nextInt(0, 3);
        return new StringJoiner(",").add(platforms[index1]).add(games[index2]).toString();
    }
}
