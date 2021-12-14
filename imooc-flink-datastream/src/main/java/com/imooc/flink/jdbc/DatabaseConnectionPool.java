package com.imooc.flink.jdbc;

import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author jucheng
 * @since 2021/12/11
 */
public class DatabaseConnectionPool {

    public static DatabaseConnectionPool INSTANCE = new DatabaseConnectionPool("jdbc:postgresql://localhost:5432/postgres", "postgres", "password");

    private final PoolingDataSource<PoolableConnection> dataSource;

    private DatabaseConnectionPool(String uri, String userName, String pwd) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }

        DriverManagerConnectionFactory driverManagerConnectionFactory
                = new DriverManagerConnectionFactory(uri, userName, pwd);

        PoolableConnectionFactory poolableConnectionFactory
                = new PoolableConnectionFactory(driverManagerConnectionFactory, null);

        ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);
        poolableConnectionFactory.setPool(connectionPool);

        this.dataSource = new PoolingDataSource<>(connectionPool);
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }
}
