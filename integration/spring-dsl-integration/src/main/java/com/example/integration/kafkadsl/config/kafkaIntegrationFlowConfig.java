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

    /** Builds the outbound Kafka flow using the configured message key. */
    @Bean
    IntegrationFlow toKafka(KafkaTemplate<?, ?> kafkaTemplate) {
        return flow -> flow.handle(
                Kafka.outboundChannelAdapter(kafkaTemplate).messageKey(this.kafkaAppProperties.messageKey()));
    }

    /**
     * Routes Kafka records to the in-memory {@code fromKafka} queue with record acknowledgments.
     * Offsets are committed after enqueueing, before downstream queue processing.
     */
    @Bean
    IntegrationFlow fromKafkaFlow(ConsumerFactory<?, ?> consumerFactory) {
        // The offset commits on hand-off to the in-memory `fromKafka` queue channel.
        return IntegrationFlow.from(Kafka.messageDrivenChannelAdapter(consumerFactory, this.kafkaAppProperties.topic())
                        .configureListenerContainer(c -> c.ackMode(AckMode.RECORD)))
                .channel(c -> c.queue("fromKafka"))
                .get();
    }
}
