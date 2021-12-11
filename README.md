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
### 编程套路
[Anatomy of Flink program](https://nightlies.apache.org/flink/flink-docs-release-1.14/docs/dev/datastream/overview/#anatomy-of-a-flink-program)
1. Obtain an execution environment,
2. Load/create the initial data,
3. Specify transformations on this data,
4. Specify where to put the results of your computations,
5. Trigger the program execution
### DataStream
```java
// get execution environment
SStreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
// ... transformation
// execute
env.execute("your job name");
```
### DataSet
```java
// get execution environment
ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
// ... transformation & sink
// no need to execute env
```
### Flink中的并行度
[Flink parallelism](https://cloud.tencent.com/developer/article/1613761)
Priority in descend:  
1. operator level
2. environment level (while getting the env)
3. client level (while executing job)
4. system level (while submitting job, reading from config)

## Deployment
### Infrastructure
主从结构
1. JobManager；主节点；高可用，HA；负责调度
2. TaskManager；从节点；多个节点；负责运行作业
