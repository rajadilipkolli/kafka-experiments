package com.example.integration.kafkadsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.integration.kafkadsl.config.KafkaAppProperties;
import com.example.integration.kafkadsl.config.KafkaGateway;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {ContainerConfiguration.class})
@TestPropertySource(properties = {"app.kafka.queue-capacity=5", "spring.kafka.consumer.properties.max.poll.records=2"})
class SlowConsumerIntegrationTest {

    @Autowired
    private KafkaGateway kafkaGateway;

    @Autowired
    private KafkaAppProperties kafkaAppProperties;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private QueueChannel fromKafka;

    @Test
    void testSlowConsumerWithBoundedQueue() throws InterruptedException {
        int messageCount = 15;
        int capacity = 5;

        // Publish more messages than capacity
        for (int i = 0; i < messageCount; i++) {
            kafkaTemplate.send(kafkaAppProperties.topic(), "slow-msg-" + i);
        }

        // Wait until messages start arriving and assert that queue size never exceeds capacity
        await().pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofSeconds(10))
                .until(() -> fromKafka.getQueueSize() > 0);

        for (int i = 0; i < 20; i++) {
            assertThat(fromKafka.getQueueSize()).isLessThanOrEqualTo(capacity);
            Thread.sleep(100);
        }

        // Drain all messages with receiveFromKafka() and assert no loss
        List<String> drained = new ArrayList<>();
        for (int i = 0; i < messageCount; i++) {
            Message<?> received = kafkaGateway.receiveFromKafka();
            assertThat(received).isNotNull();
            drained.add((String) received.getPayload());
        }

        assertThat(drained).hasSize(messageCount);
        for (int i = 0; i < messageCount; i++) {
            assertThat(drained).contains("slow-msg-" + i);
        }
    }
}
