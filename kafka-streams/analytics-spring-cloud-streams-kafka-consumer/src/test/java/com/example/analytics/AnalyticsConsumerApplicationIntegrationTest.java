/* Licensed under Apache-2.0 2019-2025 */
package com.example.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.analytics.common.AbstractIntegrationTest;
import com.example.analytics.model.PageViewEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.mock.web.MockHttpServletResponse;

class AnalyticsConsumerApplicationIntegrationTest extends AbstractIntegrationTest {

    private final SecureRandom random = new SecureRandom();
    private final String[] pages = {"home", "products", "checkout", "cart", "about"};

    @BeforeEach
    void setUpData() throws JsonProcessingException, InterruptedException {
        // Publish some test events to Kafka
        for (int i = 0; i < 5; i++) {
            PageViewEvent pageViewEvent =
                    new PageViewEvent(
                            "user" + (i + 1),
                            pages[Math.abs(random.nextInt()) % pages.length],
                            Math.abs(random.nextInt(100) + 1));

            Message<String> message =
                    MessageBuilder.withPayload(objectMapper.writeValueAsString(pageViewEvent))
                            .setHeader(KafkaHeaders.TOPIC, "pvs")
                            .build();

            kafkaTemplate.send(message);
        }

        // Wait for processing to complete
        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    void verifyProcessing() {
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(
                        () -> {
                            // Call the controller endpoint to get the processed data
                            MockHttpServletResponse response =
                                    mockMvc.perform(get("/api/page-counts"))
                                            .andExpect(status().isOk())
                                            .andReturn()
                                            .getResponse();

                            // Verify the response contains our data
                            String content = response.getContentAsString();
                            assertThat(content).isNotEmpty();
                            // Further assertions based on expected data structure
                            assertThat(content).contains("\"about\"");
                            assertThat(content).contains("\"checkout\"");
                            assertThat(content).contains("\"cart\"");
                            assertThat(content).contains("\"home\"");
                            assertThat(content).contains("\"products\"");
                        });
    }
}
