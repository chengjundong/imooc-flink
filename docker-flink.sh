FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager"
docker network create flink-network

docker run \
    --rm \
    --name=jobmanager \
    --network flink-network \
    --publish 8081:8081 \
    --env FLINK_PROPERTIES="${FLINK_PROPERTIES}" \
    flink:1.13.2-scala_2.12-java11 jobmanager

docker run \
    --rm \
    --name=taskmanager \
    --network flink-network \
    --env FLINK_PROPERTIES="${FLINK_PROPERTIES}" \
    flink:1.13.2-scala_2.12-java11 taskmanager