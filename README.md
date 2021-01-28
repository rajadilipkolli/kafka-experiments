# analytics-spring-cloud-streams-kafka

Code base of https://youtu.be/YPDzcmqwCNo


## Start docker
docker exec -it analytics-spring-cloud-streams-kafka_kafka1_1 kafka-topics --zookeeper zookeeper:2181 --create --topic my-topic --partitions 1 --replication-factor 1

You should see below response
> Created topic "my-topic"

## To verify that the three brokers are running successfully by creating another topic, this time with a replication factor of 3
docker exec -it analytics-spring-cloud-streams-kafka_kafka1_1 kafka-topics --zookeeper zookeeper:2181 --create --topic my-topic-three --partitions 1 --replication-factor 3
> Created topic "my-topic-three".


## To access Kafdrop 
 > localhost:9000

## command to remove docker
 > docker system prune -a -f --volumes
