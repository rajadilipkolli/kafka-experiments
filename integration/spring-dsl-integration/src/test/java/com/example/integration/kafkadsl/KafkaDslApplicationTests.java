package com.example.integration.kafkadsl;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.integration.kafkadsl.config.KafkaAppProperties;
import com.example.integration.kafkadsl.config.KafkaGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;

@SpringBootTest(classes = {ContainerConfiguration.class})
class KafkaDslApplicationTests {

    @Autowired
    KafkaGateway kafkaGateway;

    @Autowired
    KafkaAppProperties kafkaAppProperties;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void sendMessageToKafka() {
        String message = "test-message";
        kafkaGateway.sendToKafka(message, kafkaAppProperties.topic());
        Message<?> received = kafkaGateway.receiveFromKafka();
        assertThat(received.getPayload()).isEqualTo(message);
    }

    @Test
    void receiveMessageFromKafka() {
        String message = "test-message";
        kafkaTemplate.send(kafkaAppProperties.topic(), message);
        Message<?> received = kafkaGateway.receiveFromKafka();
        assertThat(received.getPayload()).isEqualTo(message);
    }

    @Test
    void sendAndReceiveMultipleMessages() {
        for (int i = 0; i < 10; i++) {
            String message = "message" + i;
            kafkaGateway.sendToKafka(message, kafkaAppProperties.topic());
        }
        for (int i = 0; i < 10; i++) {
            Message<?> received = kafkaGateway.receiveFromKafka();
            assertThat(received.getPayload()).isEqualTo("message" + i);
        }
    }

    @Test
    void sendMessageToNewTopic() {
        String message = "new-topic-message";
        kafkaGateway.sendToKafka(message, kafkaAppProperties.newTopic());
        Message<?> received = kafkaGateway.receiveFromKafka();
        assertThat(received.getPayload()).isEqualTo(message);
    }

    @Test
    void receiveMessageFromNewTopic() {
        String message = "new-topic-message";
        kafkaTemplate.send(kafkaAppProperties.newTopic(), message);
        Message<?> received = kafkaGateway.receiveFromKafka();
        assertThat(received.getPayload()).isEqualTo(message);
    }

    @Test
    void sendAndReceiveMultipleMessagesFromNewTopic() {
        for (int i = 0; i < 10; i++) {
            String message = "new-topic-message" + i;
            kafkaGateway.sendToKafka(message, kafkaAppProperties.newTopic());
        }
        for (int i = 0; i < 10; i++) {
            Message<?> received = kafkaGateway.receiveFromKafka();
            assertThat(received.getPayload()).isEqualTo("new-topic-message" + i);
        }
    }
}
