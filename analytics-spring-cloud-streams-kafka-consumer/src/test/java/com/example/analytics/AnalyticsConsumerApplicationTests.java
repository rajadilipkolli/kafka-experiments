package com.example.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.analytics.config.KafkaTestContainersConfiguration;
import com.example.analytics.model.PageViewEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Import(KafkaTestContainersConfiguration.class)
@SpringBootTest
@DirtiesContext
@Testcontainers
@AutoConfigureMockMvc
public class AnalyticsConsumerApplicationTests {

    private static final DockerImageName KAFKA_TEST_IMAGE =
            DockerImageName.parse("confluentinc/cp-kafka:5.3.6-1");

    @Container
    public static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(KAFKA_TEST_IMAGE);

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.kafka.bootstrap-servers",
                () -> KAFKA_CONTAINER.getBootstrapServers().substring(12));
    }

    @Autowired public KafkaTemplate<String, String> kafkaTemplate;

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Test
    void contextLoads() throws Exception {
        assertThat(KAFKA_CONTAINER.isRunning()).isTrue();
        // send message
        PageViewEvent pageViewEvent =
                new PageViewEvent("rName", "rPage", Math.random() > 5 ? 10 : 1000);
        String messageAsString = objectMapper.writeValueAsString(pageViewEvent);
        Message<String> message =
                MessageBuilder.withPayload(messageAsString)
                        .setHeaderIfAbsent(KafkaHeaders.TOPIC, "pvs")
                        .setHeader(KafkaHeaders.KEY, pageViewEvent.getUserId())
                        .build();

        this.kafkaTemplate.send(message);
        TimeUnit.SECONDS.sleep(30);
        this.mockMvc.perform(get("/counts")).andExpect(status().isOk());
    }
}
