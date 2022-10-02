[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=rajadileepkolli_analytics-spring-cloud-streams-kafka&metric=alert_status)](https://sonarcloud.io/dashboard?id=rajadileepkolli_analytics-spring-cloud-streams-kafka)

# Kafka-experiments

This repository contains sample projects integrating with kafka using different mechanisms available


## Starting docker

```shell
docker exec -it analytics-spring-cloud-streams-kafka_kafka1_1 kafka-topics --zookeeper zookeeper:2181 --create --topic my-topic --partitions 1 --replication-factor 1
```

You should see below response

> Created topic "my-topic"

## To verify that the three brokers are running successfully by creating another topic, this time with a replication factor of 3


```shell
docker exec -it analytics-spring-cloud-streams-kafka_kafka1_1 kafka-topics --zookeeper zookeeper:2181 --create --topic my-topic-three --partitions 1 --replication-factor 3
```

> Created topic "my-topic-three".


## To access Kafdrop 

 >  localhost:9000
 
 Code base of https://youtu.be/YPDzcmqwCNo

### Reference
[sivalabs](https://github.com/sivaprasadreddy/kafka-tutorial)
