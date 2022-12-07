package com.example.analytics.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@ConfigurationProperties(prefix = "io.confluent.developer.topic")
public record AnalyticsApplicationProperties(@NotBlank String topicNamePvs, @Positive short replication,
                                             @Positive short partitions) {
}
