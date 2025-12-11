package com.example.integration.kafkadsl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration(proxyBeanMethods = false)
class kafkaIntegrationFlowConfig {

    private final KafkaAppProperties kafkaAppProperties;

    kafkaIntegrationFlowConfig(KafkaAppProperties kafkaAppProperties) {
        this.kafkaAppProperties = kafkaAppProperties;
    }

    @Bean
    IntegrationFlow toKafka(KafkaTemplate<?, ?> kafkaTemplate) {
        return flow -> flow.handle(
                Kafka.outboundChannelAdapter(kafkaTemplate).messageKey(this.kafkaAppProperties.messageKey()));
    }

    @Bean
    IntegrationFlow fromKafkaFlow(ConsumerFactory<?, ?> consumerFactory) {
        return IntegrationFlow.from(Kafka.messageDrivenChannelAdapter(consumerFactory, this.kafkaAppProperties.topic()))
                .channel(c -> c.queue("fromKafka"))
                .get();
    }
}
