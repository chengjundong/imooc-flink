package com.imooc.flink.jdbc;

import org.apache.flink.api.common.functions.AbstractRichFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author jucheng
 * @since 2021/12/15
 */
public class DatabaseWordCountSink extends AbstractRichFunction implements SinkFunction<Tuple2<String, Integer>> {

    @Override
    public void open(Configuration parameters) throws Exception {
        System.out.println(">>>>>>>>>> open data sink:" + Thread.currentThread().getId());
    }

    @Override
    public void close() throws Exception {
        System.out.println(">>>>>>>>>> close data sink:" + Thread.currentThread().getId());
    }

    @Override
    public void invoke(Tuple2<String, Integer> value, Context context) throws Exception {
        try (final Connection connection = DatabaseConnectionPool.INSTANCE.getConnection();
             final PreparedStatement insert = connection.prepareStatement("INSERT INTO flink_word_count_result VALUES (?, ?)");
             final PreparedStatement update = connection.prepareStatement("UPDATE flink_word_count_result SET count = ? WHERE value = ?")) {
            update.setInt(1, value.f1);
            update.setString(2, value.f0);
            update.executeUpdate();

            // for the 1st record, update count is 0; then we use insert
            if (0 == update.getUpdateCount()) {
                insert.setString(1, value.f0);
                insert.setInt(2, value.f1);
                insert.executeUpdate();
                System.out.println(String.format(">>>>>>>> insert %s, count: %d, worker: %d",
                        value, insert.getUpdateCount(), Thread.currentThread().getId()));
            } else {
                System.out.println(String.format(">>>>>>>> update %s, count: %d, worker: %d",
                        value, update.getUpdateCount(), Thread.currentThread().getId()));
            }
        }
    }
}
