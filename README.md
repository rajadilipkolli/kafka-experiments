[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/rajadilipkolli/kafka-experiments)

# Kafka-experiments

**Apache Kafka** is an open-source, distributed streaming platform that enables real-time processing of data streams. It is designed to handle high-throughput, low-latency processing of large volumes of data, making it well-suited for use cases such as real-time analytics, event-driven architectures, and data pipelines.

Kafka is based on a publish-subscribe model, in which producers send data to Kafka topics and consumers subscribe to those topics to receive the data. Kafka stores data in a distributed, partitioned, and replicated log structure, allowing it to scale horizontally and tolerate failures.

Kafka has a number of key features that make it a popular choice for data processing:

  - **Scalability**: Kafka is designed to handle a large volume of data and can scale to handle millions of messages per second.

  - **Durability**: Kafka stores messages on disk, making it possible to recover from failures and maintain data integrity.

  - **Low latency**: Kafka is designed for low-latency processing, making it suitable for real-time applications.

  - **High-throughput**: Kafka is able to handle high-throughput data streams, allowing it to process large amounts of data in real-time.

  - **Flexibility**: Kafka is highly flexible and can be used for a wide range of data processing use cases, including real-time analytics, data pipelines, and event-driven architectures.

This repository contains sample projects integrating with kafka using different mechanisms available

  - [sample](./kafka-sample)
  - [streams](./kafka-streams)
  - [avro](./spring-boot-kafka-avro)
  - others


### Reference

Copied and modified from 
 - [sivalabs](https://github.com/sivaprasadreddy/kafka-tutorial)
 - https://github.com/timtebeek/kafka-dead-letter-publishing
