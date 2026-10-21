package com.example.integration.kafkadsl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration(proxyBeanMethods = false)
public class kafkaIntegrationFlowConfig {

    private final KafkaAppProperties kafkaAppProperties;

    kafkaIntegrationFlowConfig(KafkaAppProperties kafkaAppProperties) {
        this.kafkaAppProperties = kafkaAppProperties;
    }

    /** Returns an in-memory receive queue with the configured message capacity. */
    @Bean
    public QueueChannel fromKafka() {
        return new QueueChannel(this.kafkaAppProperties.queueCapacity());
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
     * Each enqueue waits up to five seconds; retryable delivery failures are retried without
     * an attempt limit at 100-millisecond intervals.
     */
    @Bean
    IntegrationFlow fromKafkaFlow(ConsumerFactory<?, ?> consumerFactory, QueueChannel fromKafka) {
        // The offset commits on hand-off to the in-memory `fromKafka` queue channel.
        return IntegrationFlow.from(Kafka.messageDrivenChannelAdapter(consumerFactory, this.kafkaAppProperties.topic())
                        .sendTimeout(5000L)
                        .configureListenerContainer(c -> {
                            c.ackMode(AckMode.RECORD);
                            c.errorHandler(
                                    new DefaultErrorHandler(new FixedBackOff(100L, FixedBackOff.UNLIMITED_ATTEMPTS)));
                        }))
                .channel(fromKafka)
                .get();
    }
}
