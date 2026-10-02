package com.example.integration.kafkadsl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties.AckMode;

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
        // The offset commits on hand-off to the in-memory `fromKafka` queue channel.
        return IntegrationFlow.from(Kafka.messageDrivenChannelAdapter(
                        Kafka.container(consumerFactory, this.kafkaAppProperties.topic())
                                .ackMode(AckMode.RECORD)))
                .channel(c -> c.queue("fromKafka"))
                .get();
    }
}
