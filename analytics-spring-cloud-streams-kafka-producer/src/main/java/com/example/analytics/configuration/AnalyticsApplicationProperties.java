package com.example.analytics.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

// to use the constructor binding, we need to explicitly enable our configuration class either with
// @EnableConfigurationProperties or with @ConfigurationPropertiesScan.
@ConstructorBinding
@ConfigurationProperties(prefix = "io.confluent.developer.topic")
public record AnalyticsApplicationProperties(@NotBlank String topicNamePvs, @Positive short replication,
                                             @Positive short partitions) {
}
