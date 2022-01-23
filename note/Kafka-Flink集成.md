# Run Flink in Docker
[Run flink in Docker](https://ci.apache.org/projects/flink/flink-docs-master/docs/deployment/resource-providers/standalone/docker/)
# Run Kafka
## Prerequisite
1. clone [wurstmeister/kafka-docker](https://github.com/wurstmeister/kafka-docker) to local
2. open docker-compose.yml
3. under `kafka`, add `image: wurstmeister/kafka`, comment `# build: .`. Refer to [wurstmeister/kafka-docker/issues/529](https://github.com/wurstmeister/kafka-docker/issues/529)
4. download kafka from [apache/kafka](https://dlcdn.apache.org/kafka/)
5. configure kafka/bin/windows as KAFKA_HOME in your local
## Steps
1. go to kafka-docker folder in local
2. use `ping host.docker.internal` to get docker IP
3. change `KAFKA_ADVERTISED_HOST_NAME` in docker-compose.yml
4. run `docker-compose up`  
5. start kafka manager `docker run -it --rm -p 9000:9000 -e ZK_HOSTS="${host.docker.internal}:2181" -e APPLICATION_SECRET=letmein sheepkiller/kafka-manager`
# Use script to test
First, you need register KAFKA_HOME in your system.
## Create topic
```ps1
# use powershell script "create-topic.ps1"
# the 1st param is ZK host
# the 2nd param is topic name
.\create-topic.ps1 192.168.10.124:2181 mytesttopic
# alternative
kafka-topics.bat --create --topic jared-test-20220118 --bootstrap-server 192.168.1.3:52452 --partitions 3 --replication-factor 1
```
## Produce event
```ps1
# use powershell script "produce-message.ps1"
# the 1st param is KAFKA broker host
# the 2nd param is topic name
.\produce-message.ps1 192.168.10.124:57139 mytesttopic
 #>
# alternative
kafka-console-producer.bat --bootstrap-server 192.168.1.3:52452 --topic jared-test-20220118
```

## Consumer event
```ps1
kafka-console-consumer.bat --bootstrap-server 192.168.1.3:52452 --topic jared-test-20220118
```