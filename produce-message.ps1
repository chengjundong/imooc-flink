param (
    [string]$kafkaHost,
    [string]$topic,
    [string]$kafkaHome="C:\jared\kafka\bin\windows"
)

# output parameters
Write-Host "kafka-host: $kafkaHost"
Write-Host "topic-name: $topic"
# push locatin and go to kafka home
Push-Location $kafkaHome
# create topic
.\kafka-console-producer.bat --bootstrap-server $kafkaHost --topic $topic
# go back
Pop-Location