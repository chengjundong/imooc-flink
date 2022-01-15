# Flink + ClickHouse集成

# Static data analyze -- offline
Try to use static data to do analyze

## Requirement 1
**Analyze new/old user behavior by operating system**  
key words:
1. os: operating system
2. nu: 1 = new user; other = old user

## Requirement 2
**Analyze new/old user behavior by province**
To use AMap to parse IP and get province data. Due to sync get API might block the processing, we need apply Async Mode as well

## Summary
In daily work, we might analyze data from different analog. These requirements have many common steps like data clean, data mapping, etc. We need extract common part into a UDF.

# 参考网站
[友盟+](https://www.umeng.com/)  
[神策数据](https://sademo.cloud.sensorsdata.cn/)  
[Win10 + Clickhouse环境搭建](https://www.cnblogs.com/throwable/p/14015092.html)
[高德地图API](https://lbs.amap.com/api/webservice/guide/api/staticmaps/)
[Flink Async Operator](https://nightlies.apache.org/flink/flink-docs-release-1.14/docs/dev/datastream/operators/asyncio/)