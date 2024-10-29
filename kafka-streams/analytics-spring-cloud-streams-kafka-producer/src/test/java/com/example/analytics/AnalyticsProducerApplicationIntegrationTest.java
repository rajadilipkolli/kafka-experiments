/* Licensed under Apache-2.0 2021-2023 */
package com.example.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import com.example.analytics.common.ContainersConfiguration;
import com.example.analytics.model.PageViewEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest(classes = ContainersConfiguration.class)
class AnalyticsProducerApplicationIntegrationTest {

    @Autowired KafkaTemplate<String, String> kafkaTemplate;

    private final CountDownLatch messagesLatch = new CountDownLatch(100);

    @Test
    void contextLoads() {
        assertThat(kafkaTemplate).isNotNull();
        await().pollDelay(1, TimeUnit.SECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(messagesLatch.getCount()).isLessThanOrEqualTo(99));
    }

    @KafkaListener(topics = "pvs", groupId = "pcs")
    public void listenMessages(String message) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        PageViewEvent value = objectMapper.readValue(message, PageViewEvent.class);
        messagesLatch.countDown();
        assertThat(value).isNotNull();
        assertThat(value.getDuration()).isIn(List.of(10L, 1000L));
        assertThat(value.getPage())
                .isNotBlank()
                .isIn(List.of("blog", "sitemap", "initializr", "news", "colophon", "about"));
        assertThat(value.getUserId())
                .isNotBlank()
                .isIn(List.of("rajesh", "kumar", "raja", "dilip", "chowdary", "kolli"));
    }
}
