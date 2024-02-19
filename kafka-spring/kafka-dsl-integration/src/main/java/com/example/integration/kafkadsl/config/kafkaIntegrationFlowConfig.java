package com.example.integration.kafkadsl.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration(proxyBeanMethods = false)
public class kafkaIntegrationFlowConfig {

    private final KafkaAppProperties properties;

    public kafkaIntegrationFlowConfig(KafkaAppProperties properties) {
        this.properties = properties;
    }

    @Bean
    KafkaAdmin.NewTopics topics() {
        return new KafkaAdmin.NewTopics(
                new NewTopic(properties.topic(), 1, (short) 1), new NewTopic(properties.newTopic(), 1, (short) 1));
    }

    @Bean
    IntegrationFlow toKafka(KafkaTemplate<?, ?> kafkaTemplate) {
        return flow ->
                flow.handle(Kafka.outboundChannelAdapter(kafkaTemplate).messageKey(this.properties.messageKey()));
    }

    @Bean
    IntegrationFlow fromKafkaFlow(ConsumerFactory<?, ?> consumerFactory) {
        return IntegrationFlow.from(Kafka.messageDrivenChannelAdapter(consumerFactory, this.properties.topic()))
                .channel(c -> c.queue("fromKafka"))
                .get();
    }
}
