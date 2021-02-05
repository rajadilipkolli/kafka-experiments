package com.example.analytics.configuration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

// To use the constructor binding, we need to explicitly enable our configuration class either with
// @EnableConfigurationProperties or with @ConfigurationPropertiesScan.
@ConstructorBinding
@ConfigurationProperties(prefix = "io.confluent.developer.topic")
public record AnalyticsApplicationProperties(
    @NotBlank String topicNamePvs,
    @NotBlank String topicNamePcs,
    @NotBlank String topicNameChangelog,
    @NotBlank String topicNameRePartition,
    @Positive short replication,
    @Positive short partitions) {}
