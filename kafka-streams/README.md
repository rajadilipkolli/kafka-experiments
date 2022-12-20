## Spring Cloud Integration with kafka Streams

Kafka Streams is a Java library for building real-time, scalable, and fault-tolerant data processing applications that can be integrated with Apache Kafka. It allows developers to process, transform, and analyze data streams in real-time using familiar stream processing concepts and APIs. Kafka Streams provides a variety of operations and functions for transforming and aggregating data streams, and can be used for a wide range of applications such as real-time data analytics, fraud detection, and data filtering.

Spring Cloud is a framework that helps developers to build cloud-native microservices for distributed systems. It offers a set of tools and libraries for building and deploying microservices, including a messaging system, service registry, load balancer, and API gateway.

Integrating Spring Cloud with Kafka Streams allows developers to build scalable and resilient microservices that can process real-time data streams using the power of Apache Kafka. With Spring Cloud and Kafka Streams, developers can build powerful data pipelines and applications that can handle large amounts of data and handle failures gracefully.

To integrate Spring Cloud with Kafka Streams, developers can use the Spring Cloud Stream Kafka Binder, which provides a set of libraries and tools for integrating Spring Cloud applications with Apache Kafka. This includes support for publishing and consuming messages, as well as support for stream processing using Kafka Streams.

Using Spring Cloud and Kafka Streams together allows developers to build scalable and resilient distributed systems that can handle large amounts of data and handle failures gracefully. It also enables developers to build powerful data pipelines and applications that can process real-time data streams in near real-time.


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
