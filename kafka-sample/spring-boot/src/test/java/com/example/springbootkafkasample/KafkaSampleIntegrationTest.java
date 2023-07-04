package com.example.springbootkafkasample;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.springbootkafkasample.dto.MessageDTO;
import com.example.springbootkafkasample.service.listener.Receiver2;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = TestBootKafkaSampleApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KafkaSampleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Receiver2 receiver2;

    @Test
    @Order(1)
    void testSendAndReceiveMessage() throws Exception {
        this.mockMvc
                .perform(post("/messages")
                        .content(this.objectMapper.writeValueAsString(new MessageDTO("test_1", "junitTest")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 4 from topic1 and 3 from topic2 on startUp, plus 1 from test
        await().pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(receiver2.getLatch().getCount()).isEqualTo(2));
        assertThat(receiver2.getDeadLetterLatch().getCount()).isEqualTo(1);
    }

    @Test
    @Order(2)
    void testSendAndReceiveMessageInDeadLetter() throws Exception {
        this.mockMvc
                .perform(post("/messages")
                        .content(this.objectMapper.writeValueAsString(new MessageDTO("test_1", "")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        await().pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() ->
                        assertThat(receiver2.getDeadLetterLatch().getCount()).isZero());
    }

    @Test
    void testTopicsWithPartitionsCount() throws Exception {
        this.mockMvc
                .perform(get("/topics").param("showInternalTopics", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("size()", is(7)))
                .andExpect(jsonPath("$.test_2-dlt").value(1))
                .andExpect(jsonPath("$.test_3").value(1))
                .andExpect(jsonPath("$.test_2").value(1))
                .andExpect(jsonPath("$.test_1").value(1))
                .andExpect(jsonPath("$.test_2-retry-0").value(1))
                .andExpect(jsonPath("$.__consumer_offsets").value(1))
                .andExpect(jsonPath("$.test_2-retry-1").value(1));
    }
}
