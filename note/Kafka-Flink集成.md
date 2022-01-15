# Run Flink in Docker
[Run flink in Docker](https://ci.apache.org/projects/flink/flink-docs-master/docs/deployment/resource-providers/standalone/docker/)
# Run Kafka
1. go to kafka-docker folder in local
2. use `ping host.docker.internal` to get docker IP
3. change `KAFKA_ADVERTISED_HOST_NAME` in docker-compose.yml
4. run `docker-compose up`  
5. start kafka manager `docker run -it --rm -p 9000:9000 -e ZK_HOSTS="${host.docker.internal}:2181" -e APPLICATION_SECRET=letmein sheepkiller/kafka-manager`
## Create topic
```powershell
# use powershell script "create-topic.ps1"
# the 1st param is ZK host
# the 2nd param is topic name
.\create-topic.ps1 192.168.10.124:2181 mytesttopic
```
## Produce message
```powershell
# use powershell script "produce-message.ps1"
# the 1st param is KAFKA broker host
# the 2nd param is topic name
.\produce-message.ps1 192.168.10.124:57139 mytesttopic
 #>
```