# 基本概念
在streaming数据处理中，真实应用一般以数据自带的时间戳（event time）为依据。（相较于Flink的接收时间processing time）  
由于真实环境中，流入flink的数据难以保证以绝对升序排列（monotonously ascending）。我们需要一个在一定范围内处理乱序数据的手段。  
Watermark就是用于处理此类场景。
## Watermark如何工作
使用以下表格说明watermark如何工作
- event：数据编号
- event time：数据自带的时间戳，此处不讨论如何提取时间戳
- realistic time：现实世界的时间，绝对升序排列
- 窗口宽度为5秒，`[12:00, 12:05)`
- watermark duration为1秒

| event | event time | realistic time |
| --- | --- | --- |
| #1 | 12:00 | 12:00 |
| #2 | 12:03 | 12:01 |
| #3 | 12:04 | 12:02 |
| #4 | 12:01 | 12:03 |
| #5 | 12:02 | 12:04 |
| #6 | 12:06 | 12:05 |
| <font color="#dc143c">#7</font> | <font color="#dc143c">12:03</font> | <font color="#dc143c">12:08</font> |

可以看到，event#1的event time和现实世界一样，此时也是该窗口的开端。在12：00到12：04之间，flink收到5个event，虽然乱序，但event time均落在窗口中。  
由于watermark duration为1秒，所以当event#6收到后，watermark打上 `12:05 = event#6.event_time - lateness = 12:06 - 1s`，flink认为既然12：06的消息都来了，那么12：05前的消息肯定到齐了。  
不幸的是，event#7在12:08时进入flink，可是它的event time是12：03，应该是要被上一个窗口处理。可是，watermark已经打上，上一个窗口不再处理它了。就像人生中擦肩而过的人，错过就是永远。

## 创建Watermark
`WatermarkGenerator` 有两个方法：
- onPeriodicEmit：周期性地轮询，间隔由 `ExecutionConfig.getAutoWatermarkInterval()`控制
- onEvent：由输入event触发

__以下举例三种watermark创建方式__

有界无序watermark：
1. 创建时接收一个duration参数，作为watermark创建的依据之一，不可改
2. 创建时维护一个max timestamp，用于保存接收到的所有event里最大的event time
3. 按照周期轮询执行 `onPeriodicEmit` 创建watermark。公式为 `watermark = max_timestamp - duration - 1`，此处 `-1` 用于创造一个左闭右开的区间。
4. 一旦有event输入，`onEvent` 启动，比较并更新max timestamp

绝对单调递增的watermark：
1. 一旦收到消息，在 `onEvent` 中更新watermark。因为数据的event time绝对单调递增，可以认为watermark就像数据库的序列一样一直在增长。
2. 无需 `onPeriodicEmit`

固定间隔watermark：
1. 每隔一段时间，`onPeriodicEmit`启动，将当前系统时间作为watermark
2. 无视 `onEvent`，消息输入对watermark不造成影响，仅按照现实世界时间创建watermark

## Watermark控制窗口关闭
当watermark大于等于上一个窗口的边界，触发该窗口执行。  
比如，窗口区间是 `[12:00, 12:04)`，此时，watermark被标记为 `12:06`。则触发窗口执行，如process function、agg function的getResult等。

# 进阶：如何处理被丢弃的数据
由于watermark的存在，超时到达的数据就会被丢弃。这些数据本身是有业务含义的，根据不同的应用场景，数据是否真的可以完全丢弃是不一定的。  
如果重要的业务数据被丢弃了，会造成最终结果的不准确。  
使用[旁路输出（side output）](https://nightlies.apache.org/flink/flink-docs-release-1.14/zh/docs/dev/datastream/side_output/) 获取到超时未被处理的数据。

# 进阶：lateness
不同于watermark的duration，lateness的含义是 `当watermark通知窗口可以关闭时，等待X时间后关闭`。  
默认值是0，即watermark说“到点了，关闭”，窗口就立刻关闭。可以调用window的 `allowedLateness`设置延迟时间，窗口在收到命令会开启countdown等待到时间再关闭，相当于多一层容错。  
## 思考: watermark的duration和window的lateness区别？
区别可以从调整参数来比较。  

| watermark | duration | lateness | effect |
| --- | --- | --- | --- |
| BoundedOutOfOrder | +10s | 0s | watermark晚10秒打上，当收到比窗口下限晚10秒的数据时才触发窗口关闭 |
| BoundedOutOfOrder | 0s | +10s | watermark立刻打上，但窗口不管，等待10秒再关闭，相当于收到比窗口下限晚10秒的数据后才关闭 |
| Mono Ascending | / | +10s | 对于event time绝对单调递增的，watermark永远与最新的event的event time相同，调高lateness可以处理万一可能出现的乱序情况。不过要注意，如果真的出现了乱序，watermark也乱了。 |
| Fixed Interval | 2s | +10s | 对于定长的waterwark，2秒一个watermark，增加lateness可以让窗口等待10秒再关，约等于12秒才管 |

对于 `BoundedOutOfOrder` 这样使用max timestamp减去duration作为watermark的，其实两者区别不大。

# 参考文章
- [Flink Watermark](https://nightlies.apache.org/flink/flink-docs-release-1.14/zh/docs/dev/datastream/event-time/generating_watermarks/)
- [浅析DataFlow模型](https://www.jianshu.com/p/f65e366329d7)