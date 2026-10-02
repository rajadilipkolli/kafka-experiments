/* Licensed under Apache-2.0 2021-2025 */
package com.example.analytics.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration(proxyBeanMethods = false)
class KafkaTopicsConfiguration {

    /** Declares the views topic using the configured partition and replication counts. */
    @Bean
    NewTopic pvsTopic(final AnalyticsApplicationProperties analyticsApplicationProperties) {
        return TopicBuilder.name(analyticsApplicationProperties.topicNamePvs())
                .partitions(analyticsApplicationProperties.partitions())
                .replicas(analyticsApplicationProperties.replication())
                .build();
    }
}
