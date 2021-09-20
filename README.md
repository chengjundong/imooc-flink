# Flink+ClickHouse 玩转企业级实时大数据开发
## 入门
### 大数据应用编程模型
1. 接入数据源
2. 按照业务逻辑处理数据
3. 写入存储
### 例子
- MR: InputFormat >> Mapper >> Reducer
- Hive: Table >> SQL >> insert
- Spark: RDD/DF/DS >> Transformation >> Action/Output
- Flink: Source >> Transformation >> Sink
