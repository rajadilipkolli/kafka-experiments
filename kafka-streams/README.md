## Spring Cloud Integration with kafka Streams

Kafka Streams is a Java library for building real-time, scalable, and fault-tolerant data processing applications that can be integrated with Apache Kafka. It allows developers to process, transform, and analyze data streams in real-time using familiar stream processing concepts and APIs. Kafka Streams provides a variety of operations and functions for transforming and aggregating data streams, and can be used for a wide range of applications such as real-time data analytics, fraud detection, and data filtering.

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