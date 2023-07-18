/* Licensed under Apache-2.0 2021-2023 */
package com.example.analytics.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration(proxyBeanMethods = false)
public class KafkaTopicsConfiguration {

    @Bean
    public NewTopic pvsTopic(final AnalyticsApplicationProperties analyticsApplicationProperties) {
        return TopicBuilder.name(analyticsApplicationProperties.topicNamePvs()).build();
    }
}
