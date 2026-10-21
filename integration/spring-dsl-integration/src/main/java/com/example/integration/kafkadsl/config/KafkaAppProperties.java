package com.example.integration.kafkadsl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.kafka")
public record KafkaAppProperties(
        String topic, String newTopic, String messageKey, @DefaultValue("50") int queueCapacity) {}
