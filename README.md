# Problems you might encounter in Flink + ClickHouse 
## java.io.NotSerializableException in SinkFunction
```java
// correct sink function
public class RedisT3Sink extends AbstractRichFunction implements SinkFunction<Tuple3<String, Integer, Long>> {

    private Jedis jedis;
    private final String hsetKey;

    public RedisT3Sink(String hsetKey) {
        this.hsetKey = hsetKey;
    }

    @Override
    public void open(Configuration parameters) {
        // use open method to create connection
        this.jedis = new Jedis("localhost", 6379);
    }
}

// wrong sink function
public class RedisT3Sink extends AbstractRichFunction implements SinkFunction<Tuple3<String, Integer, Long>> {

    private final Jedis jedis;
    private final String hsetKey;

    // while constructing, you will get java.io.NotSerializableException
    // because the impl of Jedis doesn't implement Serializable interface
    public RedisT3Sink(String hsetKey) {
        this.jedis = new Jedis("localhost", 6379);
        this.hsetKey = hsetKey;
    }

    @Override
    public void open(Configuration parameters) {}
}
```
reference
- [SqlSessionTemplate is not serializable in flink](https://stackoverflow.com/questions/66729576/sqlsessiontemplate-is-not-serializable-in-flink)
- [Flink: java.io.NotSerializableException: redis.clients.jedis.JedisCluster](https://stackoverflow.com/questions/56057346/flink-java-io-notserializableexception-redis-clients-jedis-jediscluster)
## Run ClickHouse in WIN10, get "tzdata, key not found" error while connecting
Use ClickHouse version 20.x