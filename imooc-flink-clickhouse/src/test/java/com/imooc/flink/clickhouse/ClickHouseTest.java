package com.imooc.flink.clickhouse;

import com.clickhouse.client.config.ClickHouseSslMode;
import com.clickhouse.jdbc.ClickHouseConnection;
import com.clickhouse.jdbc.ClickHouseDataSource;
import com.clickhouse.jdbc.ClickHouseStatement;
import ru.yandex.clickhouse.settings.ClickHouseProperties;

import java.sql.ResultSet;

/**
 * @author jucheng
 * @since 2022/1/3
 */
public class ClickHouseTest {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:clickhouse://localhost:8123/default";
        ClickHouseProperties props = new ClickHouseProperties();
        props.setUser("root");
        props.setPassword("root");
        props.setSslMode(ClickHouseSslMode.STRICT.name());

        ClickHouseDataSource dataSource = new ClickHouseDataSource(url, props.asProperties());
        try (ClickHouseConnection connection = dataSource.getConnection();
             ClickHouseStatement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT * FROM system.functions");
            while(resultSet.next()) {
                final String result = resultSet.getString("name");
                System.out.println(result);
            }
        }
    }
}
