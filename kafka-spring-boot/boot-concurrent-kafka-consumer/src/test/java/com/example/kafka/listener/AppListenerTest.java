package com.example.kafka.listener;

import static com.example.kafka.listener.AppListener.SPRING_KAFKA_TEST_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.kafka.common.ContainerConfig;
import com.example.kafka.common.ProducerConfig;
import com.example.kafka.processor.ToUpperStringProcessor;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest(
        classes = {ProducerConfig.class, ContainerConfig.class},
        properties = {"spring.kafka.producer.acks=1"})
class AppListenerTest {

    private static final int NUM_MESSAGES = 1000;
    private static final int PARTITION_KEY_DIVISOR = 10;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    @Qualifier("toUpperStringProcessors") List<ToUpperStringProcessor> toUpperStringProcessors;

    /**
     * Tests if all messages are processed by multiple StringProcessor instances.
     * Ensures that the KafkaTemplate is not null, sends a number of messages, and
     * then verifies that all messages are processed within a specified duration and
     * that each StringProcessor instance has a non-empty queue with distinct data.
     */
    @Test
    void onMessage() {
        assertThat(kafkaTemplate).isNotNull();
        CompletableFuture<?>[] futures = sendMessages();
        assertAllMessagesProcessed(futures);
        assertStringProcessorsBehaviors();
    }

    /**
     * Asserts specific behaviors of the StringProcessor instances.
     * Verifies that there are exactly two StringProcessor instances, each with a non-empty queue,
     * and that the data in the queues of these two processors are distinct.
     */
    private void assertStringProcessorsBehaviors() {
        await().untilAsserted(() -> assertThat(toUpperStringProcessors).hasSize(3));
        assertThat(toUpperStringProcessors.get(0).queueSize()).isPositive();
        assertThat(toUpperStringProcessors.get(1).queueSize()).isPositive();
        assertThat(toUpperStringProcessors.get(2).queueSize()).isPositive();
        Set<String> distinctQueuedData = toUpperStringProcessors.get(0).distinctQueuedData();
        distinctQueuedData.addAll(toUpperStringProcessors.get(2).distinctQueuedData());
        assertThat(toUpperStringProcessors.get(1).distinctQueuedData()).doesNotContainAnyElementsOf(distinctQueuedData);
    }

    /**
     * Sends a predefined number of messages to a Kafka topic.
     * Each message has a key derived from a modulus operation and a specific data payload.
     *
     * @return an array of CompletableFuture objects representing the result of each send operation.
     */
    private CompletableFuture<?>[] sendMessages() {
        return IntStream.range(0, NUM_MESSAGES)
                .mapToObj(value -> "key_" + value % PARTITION_KEY_DIVISOR)
                .map(key -> kafkaTemplate.send(SPRING_KAFKA_TEST_TOPIC, key, "data for key: " + key))
                .toArray(CompletableFuture[]::new);
    }

    /**
     * Asserts that all sent messages are processed within a specific time frame.
     *
     * @param futures an array of CompletableFuture objects representing the result of each send operation.
     */
    private void assertAllMessagesProcessed(CompletableFuture<?>[] futures) {
        assertThat(CompletableFuture.allOf(futures)).succeedsWithin(Duration.ofSeconds(10));
    }
}
