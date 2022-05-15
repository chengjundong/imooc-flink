# Flink 状态管理

# 要点
1. 使用state存储计算的中间状态，对象设置为 `transient` 变量以阻止其序列化
2. state的类型，比如list / map 等
3. 选择合适的state-backend，如JVM memory / RocksDB 等
4. 使用 `enableCheckpointing` 开启checkpoint
5. 选择合适的checkpoint存储位置，比如文件系统（S3， Ceph），RocksDB等
6. 设置合适的checkpoint参数，比如timeout / retry / 并发度 / failure tolerance等
7. 选择checkpoint的retain策略：任务结束后删除，任务结束后保留
8. checkpoint在任务失败时，flink会自动从上一个checkpoint恢复
9. 可以在flink portal上手工制作savepoint，较为耗时，数据结构类似checkpoint，可以作为手工备份使用，不会自动删除

# 参考文档
[Flink Working State](https://nightlies.apache.org/flink/flink-docs-master/docs/dev/datastream/fault-tolerance/state/)