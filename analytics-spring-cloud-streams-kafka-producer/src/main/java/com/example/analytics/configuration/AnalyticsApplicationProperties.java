package com.example.analytics.configuration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

// to use the constructor binding, we need to explicitly enable our configuration class either with
// @EnableConfigurationProperties or with @ConfigurationPropertiesScan.
@ConstructorBinding
@ConfigurationProperties(prefix = "io.confluent.developer.topic")
public class AnalyticsApplicationProperties {

  @NotBlank private final String topicNamePvs;

  @Positive private final short replication;

  @Positive private final short partitions;

  public AnalyticsApplicationProperties(
      @NotBlank String topicNamePvs, @Positive short replication, @Positive short partitions) {
    this.topicNamePvs = topicNamePvs;
    this.replication = replication;
    this.partitions = partitions;
  }

  public String getTopicNamePvs() {
    return topicNamePvs;
  }

  public short getReplication() {
    return replication;
  }

  public short getPartitions() {
    return partitions;
  }
}
