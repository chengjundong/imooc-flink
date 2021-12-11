# Flink API进阶篇
## Function interface
基础的function都实现了 `org.apache.flink.api.common.functions.Function` 接口
- MapFunction
- FlatMapFunction
- FilterFunction

增强版的function实现了 `org.apache.flink.api.common.functions.RichFunction` 接口
- RichMapFunction
- RichFilterFunction

Rich Function可以提供额外的算子全生命周期管理，以及可以获得运行时上下文 `org.apache.flink.api.common.functions.RuntimeContext`的能力。  

## 自定义source
通过实现 `org.apache.flink.streaming.api.functions.source.SourceFunction` 接口，我们可以自定义data source。  以下样例代码可以无限输出5个单词
```java
public class WordCountSource implements SourceFunction<String> {

    private boolean running = true;

    private final String[] words = new String[]{"java", "python", "c++", "golang", "PHP"};

    @Override
    public void run(SourceContext<String> ctx) throws Exception {
        for (; running; ) {
            int index = ThreadLocalRandom.current().nextInt(0, 5);
            ctx.collect(words[index]);
        }
    }

    @Override
    public void cancel() {
        this.running = false;
    }
}
```
有了上述功能后，我们就可以从任何我们想要的数据源中提取数据。比如ElasticSearch，PostgreSQL，Redis等。

## 使用Flink读取PostgreSQL的数据
流程和自定义source几乎相同，只是把内存中随机生成数据变成从数据库读取。  
参考Repository中的代码
```java
// data source connecting to postgreSQL
com.imooc.flink.jdbc.DataSource
// word count data source, reading from data base and collected by source function
com.imooc.flink.jdbc.DatabaseWordCountSource
```

## 高级算子
### Union
将多个流合并，要求数据类型一致
```java
// 分别读取两个文件，将两个流合并后输出
DataStreamSource<String> ds1 = env.readTextFile("data-file/batch-word-count.txt");DataStreamSource<String> ds2 = env.readTextFile("data-file/batch-word-count2.txt");
ds1.union(ds2).print();
```
### Connect
将两个流连接（注意只能两个），数据类型可以不一致，并且两个stream之间共享state。
```java
ArrayList<String> l1 = new ArrayList<>(){
    {
        this.add("true");
        this.add("false");
    }
};
DataStreamSource<String> ds1 = env.fromCollection(l1);

// 两个流中的数据量大小可以不一致
ArrayList<Integer> l2 = new ArrayList<>(){
    {
        this.add(1);
        this.add(2);
        this.add(3);
    }
};
ConnectedStreams<Integer, String> ds12 = env.fromCollection(l2).connect(ds1);

// 将两个流不同类型的数据，map成同一个类型
ds12.map(new CoMapFunction<Integer, String, Boolean>() {
    @Override
    public Boolean map1(Integer value) throws Exception {
        return value % 2 == 0;
    }

    @Override
    public Boolean map2(String value) throws Exception {
        return Boolean.valueOf(value);
    }
}).print();
```