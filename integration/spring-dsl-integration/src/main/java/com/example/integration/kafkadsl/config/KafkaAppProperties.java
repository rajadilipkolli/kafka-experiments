package com.example.integration.kafkadsl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.kafka")
public record KafkaAppProperties(String topic, String newTopic, String messageKey) {}
