/* Licensed under Apache-2.0 2021-2023 */
package com.example.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.analytics.model.PageViewEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Import(KafkaTestContainersConfiguration.class)
@SpringBootTest
@Testcontainers
class AnalyticsProducerApplicationIntegrationTest {

    private static final DockerImageName KAFKA_TEST_IMAGE =
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0");

    @Container @ServiceConnection
    public static final KafkaContainer KAFKA = new KafkaContainer(KAFKA_TEST_IMAGE).withKraft();

    @Test
    void contextLoads() {
        assertThat(KAFKA.isRunning()).isTrue();
    }

    @KafkaListener(topics = "pvs")
    public void listenMessages(String message) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        PageViewEvent value = objectMapper.readValue(message, PageViewEvent.class);
        assertThat(value).isNotNull();
        assertThat(value.getDuration()).isIn(List.of(10, 1000));
        assertThat(value.getPage())
                .isNotBlank()
                .isIn(List.of("blog", "sitemap", "initializer", "news"));
        assertThat(value.getUserId())
                .isNotBlank()
                .isIn(List.of("Raja", "Dilip", "Chowdary", "Kolli"));
    }
}
