param (
    [string]$zkHost,
    [string]$topic,
    [int]$partition=6
)
# output parameters
Write-Host "zk-host: $zkHost"
Write-Host "topic-name: $topic"
Write-Host "parition: $partition"
# create topic
kafka-topics.bat --create --zookeeper $zkHost --replication-factor 1 --partitions $partition --topic $topic