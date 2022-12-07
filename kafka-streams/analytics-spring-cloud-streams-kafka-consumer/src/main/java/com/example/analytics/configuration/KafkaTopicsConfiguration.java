package com.example.analytics.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfiguration {

  private final AnalyticsApplicationProperties analyticsApplicationProperties;

  public KafkaTopicsConfiguration(AnalyticsApplicationProperties analyticsApplicationProperties) {
    this.analyticsApplicationProperties = analyticsApplicationProperties;
  }

  @Bean
  public NewTopic kafkaStreamspcmvRePartitionTopic() {
    return TopicBuilder.name(analyticsApplicationProperties.topicNameRePartition())
        .partitions(analyticsApplicationProperties.partitions())
        .replicas(analyticsApplicationProperties.replication())
        .build();
  }

  @Bean
  public NewTopic kafkaStreamspcmvChangeLogTopic() {
    return TopicBuilder.name(analyticsApplicationProperties.topicNameChangelog())
        .partitions(analyticsApplicationProperties.partitions())
        .replicas(analyticsApplicationProperties.replication())
        .build();
  }

  @Bean
  public NewTopic pcsTopic() {
    return TopicBuilder.name(analyticsApplicationProperties.topicNamePcs())
        .partitions(analyticsApplicationProperties.partitions())
        .replicas(analyticsApplicationProperties.replication())
        .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
        .build();
  }

  @Bean
  public NewTopic pvsTopic() {
    return TopicBuilder.name(analyticsApplicationProperties.topicNamePvs())
        .partitions(analyticsApplicationProperties.partitions())
        .replicas(analyticsApplicationProperties.replication())
        .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
        .build();
  }
}
