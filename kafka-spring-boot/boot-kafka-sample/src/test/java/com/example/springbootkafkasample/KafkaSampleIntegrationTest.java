package com.example.springbootkafkasample;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.springbootkafkasample.common.ContainerConfig;
import com.example.springbootkafkasample.dto.KafkaListenerRequest;
import com.example.springbootkafkasample.dto.MessageDTO;
import com.example.springbootkafkasample.dto.Operation;
import com.example.springbootkafkasample.service.listener.Receiver2;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ContainerConfig.class)
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
    void sendAndReceiveMessage() throws Exception {
        long initialCount = receiver2.getLatch().getCount();
        this.mockMvc
                .perform(post("/messages")
                        .content(this.objectMapper.writeValueAsString(new MessageDTO("test_1", "junitTest")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 4 from topic1 and 3 from topic2 on startUp, plus 1 from test
        await().pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(receiver2.getLatch().getCount()).isEqualTo(initialCount - 1));
        assertThat(receiver2.getDeadLetterLatch().getCount()).isEqualTo(1);
    }

    @Test
    @Order(2)
    void sendAndReceiveMessageInDeadLetter() throws Exception {
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
    void topicsWithPartitionsCount() throws Exception {
        this.mockMvc
                .perform(get("/topics").param("showInternalTopics", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("size()", is(7)))
                .andExpect(jsonPath("$[0].topicName").value("__consumer_offsets"))
                .andExpect(jsonPath("$[0].partitionCount").value(1))
                .andExpect(jsonPath("$[0].replicationCount").value(1))
                .andExpect(jsonPath("$[1].topicName").value("test_1"))
                .andExpect(jsonPath("$[1].partitionCount").value(32))
                .andExpect(jsonPath("$[1].replicationCount").value(1))
                .andExpect(jsonPath("$[2].topicName").value("test_2"))
                .andExpect(jsonPath("$[2].partitionCount").value(1))
                .andExpect(jsonPath("$[2].replicationCount").value(1))
                .andExpect(jsonPath("$[3].topicName").value("test_2-dlt"))
                .andExpect(jsonPath("$[3].partitionCount").value(1))
                .andExpect(jsonPath("$[3].replicationCount").value(1))
                .andExpect(jsonPath("$[4].topicName").value("test_2-retry-0"))
                .andExpect(jsonPath("$[4].partitionCount").value(1))
                .andExpect(jsonPath("$[4].replicationCount").value(1))
                .andExpect(jsonPath("$[5].topicName").value("test_2-retry-1"))
                .andExpect(jsonPath("$[5].partitionCount").value(1))
                .andExpect(jsonPath("$[5].replicationCount").value(1))
                .andExpect(jsonPath("$[6].topicName").value("test_3"))
                .andExpect(jsonPath("$[6].partitionCount").value(32))
                .andExpect(jsonPath("$[6].replicationCount").value(1));
    }

    @Test
    void getListOfContainers() throws Exception {
        this.mockMvc
                .perform(get("/listeners"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(5))
                .andExpect(jsonPath("$.['org.springframework.kafka.KafkaListenerEndpointContainer#1-dlt']")
                        .value(true))
                .andExpect(jsonPath("$.['org.springframework.kafka.KafkaListenerEndpointContainer#0']")
                        .value(true))
                .andExpect(jsonPath("$.['org.springframework.kafka.KafkaListenerEndpointContainer#1']")
                        .value(true))
                .andExpect(jsonPath("$.['org.springframework.kafka.KafkaListenerEndpointContainer#1-retry-0']")
                        .value(true))
                .andExpect(jsonPath("$.['org.springframework.kafka.KafkaListenerEndpointContainer#1-retry-1']")
                        .value(true));
    }

    @Test
    void stopAndStartContainers() throws Exception {
        this.mockMvc
                .perform(post("/listeners")
                        .content(this.objectMapper.writeValueAsString(new KafkaListenerRequest(
                                "org.springframework.kafka.KafkaListenerEndpointContainer#1-dlt", Operation.STOP)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(5))
                .andExpect(jsonPath("$.['org.springframework.kafka.KafkaListenerEndpointContainer#1-dlt']")
                        .value(false))
                .andExpect(jsonPath("$.['org.springframework.kafka.KafkaListenerEndpointContainer#0']")
                        .value(true))
                .andExpect(jsonPath("$.['org.springframework.kafka.KafkaListenerEndpointContainer#1']")
                        .value(true))
                .andExpect(jsonPath("$.['org.springframework.kafka.KafkaListenerEndpointContainer#1-retry-0']")
                        .value(true))
                .andExpect(jsonPath("$.['org.springframework.kafka.KafkaListenerEndpointContainer#1-retry-1']")
                        .value(true));
        this.mockMvc
                .perform(post("/listeners")
                        .content(this.objectMapper.writeValueAsString(new KafkaListenerRequest(
                                "org.springframework.kafka.KafkaListenerEndpointContainer#1-dlt", Operation.START)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(5))
                .andExpect(jsonPath("$.['org.springframework.kafka.KafkaListenerEndpointContainer#1-dlt']")
                        .value(true))
                .andExpect(jsonPath("$.['org.springframework.kafka.KafkaListenerEndpointContainer#0']")
                        .value(true))
                .andExpect(jsonPath("$.['org.springframework.kafka.KafkaListenerEndpointContainer#1']")
                        .value(true))
                .andExpect(jsonPath("$.['org.springframework.kafka.KafkaListenerEndpointContainer#1-retry-0']")
                        .value(true))
                .andExpect(jsonPath("$.['org.springframework.kafka.KafkaListenerEndpointContainer#1-retry-1']")
                        .value(true));
    }

    @Test
    void invalidContainerOperation() throws Exception {
        this.mockMvc
                .perform(post("/listeners")
                        .content(objectMapper.writeValueAsString(
                                new KafkaListenerRequest("invalid-container-id", Operation.STOP)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void whenInvalidOperation_thenReturnsBadRequest() throws Exception {
        String invalidRequest = "{ \"containerId\": \"myListener\", \"operation\": \"INVALID\" }";

        mockMvc.perform(post("/listeners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type", CoreMatchers.is("about:blank")))
                .andExpect(jsonPath("$.title", CoreMatchers.is("Bad Request")))
                .andExpect(jsonPath("$.status", CoreMatchers.is(400)))
                .andExpect(jsonPath(
                        "$.detail", CoreMatchers.is("Invalid operation value. Allowed values are: START, STOP.")))
                .andExpect(jsonPath("$.instance", CoreMatchers.is("/listeners")));
    }
}
