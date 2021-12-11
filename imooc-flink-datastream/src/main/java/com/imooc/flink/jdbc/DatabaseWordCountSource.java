package com.imooc.flink.jdbc;

import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author jucheng
 * @since 2021/12/11
 */
public class DatabaseWordCountSource implements ParallelSourceFunction<String> {

    private boolean running = true;

    @Override
    public void run(SourceContext<String> ctx) throws Exception {
        for (; running; ) {
            int j = ThreadLocalRandom.current().nextInt(1, 6);
            String sql = String.format("SELECT * FROM flink_word_count WHERE id=%d;", j);
            try (Connection connection = DataSource.INSTANCE.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id", "value"});
                 ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    String value = resultSet.getString("value");
                    ctx.collect(value);
                }
            }
        }
    }

    @Override
    public void cancel() {
        this.running = false;
    }
}
