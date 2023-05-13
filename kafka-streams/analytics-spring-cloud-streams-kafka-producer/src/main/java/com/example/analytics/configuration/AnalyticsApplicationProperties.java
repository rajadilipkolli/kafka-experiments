/* Licensed under Apache-2.0 2021-2022 */
package com.example.analytics.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "io.confluent.developer.topic")
public record AnalyticsApplicationProperties(
        @NotBlank String topicNamePvs, @Positive short replication, @Positive short partitions) {}
