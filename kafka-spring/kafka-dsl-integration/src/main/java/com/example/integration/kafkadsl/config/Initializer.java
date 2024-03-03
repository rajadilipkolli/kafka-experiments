package com.example.integration.kafkadsl.config;

import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class Initializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);

    private final KafkaGateway kafkaGateway;
    private final KafkaAppProperties properties;
    private final IntegrationFlowContext flowContext;
    private final KafkaProperties kafkaProperties;

    public Initializer(
            KafkaGateway kafkaGateway,
            KafkaAppProperties properties,
            IntegrationFlowContext flowContext,
            KafkaProperties kafkaProperties) {
        this.kafkaGateway = kafkaGateway;
        this.properties = properties;
        this.flowContext = flowContext;
        this.kafkaProperties = kafkaProperties;
    }

    @Override
    public void run(String... args) {
        log.info("Sending 10 messages...");
        for (int i = 0; i < 10; i++) {
            String message = "foo" + i;
            log.info("Send to Kafka: :{}", message);
            kafkaGateway.sendToKafka(message, this.properties.topic());
        }
        for (int i = 0; i < 10; i++) {
            Message<?> received = kafkaGateway.receiveFromKafka();
            log.info("Received Message :{}", received);
        }
        log.info("Adding an adapter for a second topic and sending 10 messages...");
        addAnotherListenerForTopics(this.properties.newTopic());
        for (int i = 0; i < 10; i++) {
            String message = "bar" + i;
            log.info("Sent : {} to Kafka topic {}", message, properties.newTopic());
            kafkaGateway.sendToKafka(message, this.properties.newTopic());
        }
        for (int i = 0; i < 10; i++) {
            Message<?> received = kafkaGateway.receiveFromKafka();
            log.info("Received Message :{} in Kafka topic {}", received, properties.newTopic());
        }
    }

    public void addAnotherListenerForTopics(String... topics) {
        Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties(null);
        // change the group id, so we don't revoke the other partitions.
        consumerProperties.put(
                ConsumerConfig.GROUP_ID_CONFIG, consumerProperties.get(ConsumerConfig.GROUP_ID_CONFIG) + "x");
        IntegrationFlow flow = IntegrationFlow.from(Kafka.messageDrivenChannelAdapter(
                        new DefaultKafkaConsumerFactory<String, String>(consumerProperties), topics))
                .channel("fromKafka")
                .get();
        this.flowContext.registration(flow).register();
    }
}
