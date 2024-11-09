/* Licensed under Apache-2.0 2019-2023 */
package com.example.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.analytics.common.ContainersConfiguration;
import com.example.analytics.model.PageViewEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ContainersConfiguration.class)
@AutoConfigureMockMvc
class AnalyticsConsumerApplicationIntegrationTest {

    @Autowired public KafkaTemplate<String, String> kafkaTemplate;

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void setUpData() throws JsonProcessingException, InterruptedException {
        // send message
        PageViewEvent pageViewEvent =
                new PageViewEvent("rName", "rPage", new SecureRandom().nextInt(10) > 5 ? 10 : 1000);
        String messageAsString = objectMapper.writeValueAsString(pageViewEvent);
        Message<String> message =
                MessageBuilder.withPayload(messageAsString)
                        .setHeaderIfAbsent(KafkaHeaders.TOPIC, "pvs")
                        .setHeader(KafkaHeaders.KEY, pageViewEvent.getUserId())
                        .build();

        this.kafkaTemplate.send(message);
        // waiting for stream to change status from NOT_PARTITIONED to RUNNING
        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    void verifyProcessing() {

        await().pollInterval(Duration.ofSeconds(1))
                .atMost(30, TimeUnit.SECONDS)
                .untilAsserted(
                        () -> {
                            MockHttpServletResponse response =
                                    this.mockMvc
                                            .perform(get("/counts"))
                                            .andExpect(status().isOk())
                                            .andReturn()
                                            .getResponse();
                            assertThat(response.getContentAsString()).contains("{\"rPage\":1}");
                        });
    }
}
