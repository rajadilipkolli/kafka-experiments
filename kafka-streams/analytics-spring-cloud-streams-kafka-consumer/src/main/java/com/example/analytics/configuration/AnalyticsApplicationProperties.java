/* Licensed under Apache-2.0 2021-2022 */
package com.example.analytics.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;

// To use the constructor binding, we need to explicitly enable our configuration class either with
// @EnableConfigurationProperties or with @ConfigurationPropertiesScan.
@ConfigurationProperties(prefix = "application.topic")
public record AnalyticsApplicationProperties(
        @NotBlank String topicNamePvs,
        @NotBlank String topicNamePcs,
        @NotBlank String topicNameChangelog,
        @NotBlank String topicNameRePartition,
        @Positive short replication,
        @Positive short partitions) {}
