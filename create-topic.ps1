param (
    [string]$zkHost,
    [string]$topic,
    [int]$partition=6,
    [string]$kafkaHome="C:\jared\kafka\bin\windows"
)
# output parameters
Write-Host "zk-host: $zkHost"
Write-Host "topic-name: $topic"
Write-Host "parition: $partition"
# push locatin and go to kafka home
Push-Location $kafkaHome
# create topic
.\kafka-topics.bat --create --zookeeper $zkHost --replication-factor 1 --partitions $partition --topic $topic
# go back
Pop-Location