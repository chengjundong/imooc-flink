param (
    [string]$kafkaHost,
    [string]$topic
)

# output parameters
Write-Host "kafka-host: $kafkaHost"
Write-Host "topic-name: $topic"
# create topic
.\kafka-console-producer.bat --bootstrap-server $kafkaHost --topic $topic