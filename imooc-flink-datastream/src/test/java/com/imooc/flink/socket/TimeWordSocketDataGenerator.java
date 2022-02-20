package com.imooc.flink.socket;

import java.text.SimpleDateFormat;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author jucheng
 * @since 2022/2/20
 */
public class TimeWordSocketDataGenerator implements SocketDataGenerator{

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private final String[] words = new String[]{"java", "c++", "python"};

//    /**
//     * @return {{millisecond in Long}},{{word}}
//     * @throws Exception
//     */
//    @Override
//    public String generateData() throws Exception {
//        int index = ThreadLocalRandom.current().nextInt(0,3);
//        String word = words[index];
//        return new StringJoiner(",").add(String.valueOf(System.currentTimeMillis())).add(word).toString();
//    }

    /**
     * @return {{millisecond in Long}},{{word}}
     * @throws Exception
     */
    @Override
    public String generateData() throws Exception {
        TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextLong(500, 3000));
        String word = "java";
        long salt = ThreadLocalRandom.current().nextLong(-100000,0);
        long time = System.currentTimeMillis() + salt;
        System.out.println(">>>>>>>>>>>>>>> event dropped, salt is: " + salt + ", time is: " + dateFormat.format(time));
        return new StringJoiner(",").add(String.valueOf(time)).add(word).toString();
    }
}
