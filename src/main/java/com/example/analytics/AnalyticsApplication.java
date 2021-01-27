package com.example.analytics;

import com.example.analytics.binding.AnalyticsBinding;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBinding(AnalyticsBinding.class)
public class AnalyticsApplication {

  @Value("${io.confluent.developer.topic.name}")
  private String topicName;

  @Value("${io.confluent.developer.topic.partitions}")
  private int numPartitions;

  @Value("${io.confluent.developer.topic.replication}")
  private short replicationFactor;

  @Bean
  public NewTopic pvsTopic() {

    return new NewTopic(topicName, numPartitions, replicationFactor);
  }

  @Bean
  public NewTopic kafkaStreamspcmvTopic() {

    return new NewTopic("kafka-streams-analytics-pcmv-repartition", numPartitions, replicationFactor);
  }

  @Bean
  public NewTopic pcsTopic() {

    return new NewTopic("pcs", numPartitions, replicationFactor);
  }

  public static void main(String[] args) {
    SpringApplication.run(AnalyticsApplication.class, args);
  }

}
