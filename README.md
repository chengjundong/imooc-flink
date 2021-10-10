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

## Deployment
### Infrastructure
主从结构
1. JobManager；主节点；高可用，HA；负责调度
2. TaskManager；从节点；多个节点；负责运行作业
### Run Flink in Docker
[Run flink in Docker](https://ci.apache.org/projects/flink/flink-docs-master/docs/deployment/resource-providers/standalone/docker/)
### Run Kafka
go to kafka-docker folder  
run `docker-compose up`  
you might need change the `KAFKA_ADVERTISED_HOST_NAME`, use `ping host.docker.internal`  
start kafka manager  
`docker run -it --rm -p 9000:9000 -e ZK_HOSTS="192.168.1.18:2181" -e APPLICATION_SECRET=letmein sheepkiller/kafka-manager`  
ZK_HOSTS might be changed
access kafka manager: http://192.168.1.18:9000/
