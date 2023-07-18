/* Licensed under Apache-2.0 2021-2023 */
package com.example.analytics.configuration;

import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin.NewTopics;

@Configuration(proxyBeanMethods = false)
public class KafkaTopicsConfiguration {

    @Bean
    NewTopics kafkaTopics(final AnalyticsApplicationProperties analyticsApplicationProperties) {
        return new NewTopics(
                // pcsTopic
                TopicBuilder.name(analyticsApplicationProperties.topicNamePcs()).compact().build(),
                // pvsTopic
                TopicBuilder.name(analyticsApplicationProperties.topicNamePvs())
                        .replicas(analyticsApplicationProperties.replication())
                        .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
                        .build());
    }
}
