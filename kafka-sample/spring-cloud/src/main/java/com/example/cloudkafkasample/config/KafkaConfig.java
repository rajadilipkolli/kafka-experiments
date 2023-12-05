/* (C)2023 */
package com.example.cloudkafkasample.config;

import org.springframework.cloud.stream.config.ListenerContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;

@Configuration(proxyBeanMethods = false)
public class KafkaConfig {

    @Bean
    ListenerContainerCustomizer<AbstractMessageListenerContainer<byte[], byte[]>> customizer(
            DefaultErrorHandler errorHandler) {
        return (container, dest, group) -> {
            container.setCommonErrorHandler(errorHandler);
        };
    }

    @Bean
    DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
        return new DefaultErrorHandler(deadLetterPublishingRecoverer);
    }

    @Bean
    DeadLetterPublishingRecoverer publisher(KafkaOperations<byte[], byte[]> bytesTemplate) {
        return new DeadLetterPublishingRecoverer(bytesTemplate);
    }
}
