package com.imooc.flink.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.flink.pojo.UserAccountBalance;

import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author jucheng
 * @since 2022/2/12
 */
public class UserBalanceSocketDataGenerator implements SocketDataGenerator{

    private final ObjectMapper jackson2 = new ObjectMapper();
    private final DecimalFormat format = new DecimalFormat("#.##");
    private final long[] users = new long[]{
            3000000, 3000001, 3000002, 3000003, 3000005,
            4000000, 4000001, 4000002, 4000003, 4000005};

    @Override
    public String generateData() throws Exception {
        // 1 ~ 3
        int accountType = ThreadLocalRandom.current().nextInt(1,4);
        String balance = format.format(ThreadLocalRandom.current().nextDouble(-100000, 100000));
        long userId = users[ThreadLocalRandom.current().nextInt(0, 10)];
        int unitId = 3 == userId / 1000000 ? 840 : 978;

        return jackson2.writeValueAsString(new UserAccountBalance(userId, accountType, balance, unitId));
    }
}
