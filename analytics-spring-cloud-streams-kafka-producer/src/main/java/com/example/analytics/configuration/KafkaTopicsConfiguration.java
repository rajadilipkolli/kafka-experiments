package com.example.analytics.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicsConfiguration {

  private final AnalyticsApplicationProperties analyticsApplicationProperties;

  public KafkaTopicsConfiguration(AnalyticsApplicationProperties analyticsApplicationProperties) {
    this.analyticsApplicationProperties = analyticsApplicationProperties;
  }

  @Bean
  public NewTopic pvsTopic() {
    return new NewTopic(
        analyticsApplicationProperties.getTopicNamePvs(),
        analyticsApplicationProperties.getPartitions(),
        analyticsApplicationProperties.getReplication());
  }
}
