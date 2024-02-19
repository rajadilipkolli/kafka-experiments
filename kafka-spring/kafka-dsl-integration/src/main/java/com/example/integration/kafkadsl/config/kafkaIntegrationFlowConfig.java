package com.example.integration.kafkadsl.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration(proxyBeanMethods = false)
public class kafkaIntegrationFlowConfig {

    private final KafkaAppProperties properties;

    public kafkaIntegrationFlowConfig(KafkaAppProperties properties) {
        this.properties = properties;
    }

    @Bean
    public NewTopic topic(KafkaAppProperties properties) {
        return new NewTopic(properties.getTopic(), 1, (short) 1);
    }

    @Bean
    public NewTopic newTopic(KafkaAppProperties properties) {
        return new NewTopic(properties.getNewTopic(), 1, (short) 1);
    }

    @Bean
    public IntegrationFlow toKafka(KafkaTemplate<?, ?> kafkaTemplate) {
        return f -> f
                .handle(Kafka.outboundChannelAdapter(kafkaTemplate)
                        .messageKey(this.properties.getMessageKey()));
    }

    @Bean
    public IntegrationFlow fromKafkaFlow(ConsumerFactory<?, ?> consumerFactory) {
        return IntegrationFlow
                .from(Kafka.messageDrivenChannelAdapter(consumerFactory, this.properties.getTopic()))
                .channel(c -> c.queue("fromKafka"))
                .get();
    }
}
