package com.example.springbootkafkasample;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.springbootkafkasample.common.ContainerConfig;
import com.example.springbootkafkasample.dto.KafkaListenerRequest;
import com.example.springbootkafkasample.dto.MessageDTO;
import com.example.springbootkafkasample.dto.Operation;
import com.example.springbootkafkasample.service.listener.Receiver2;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest(classes = {ContainerConfig.class})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KafkaSampleIntegrationTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Receiver2 receiver2;

    @Test
    @Order(101)
    void sendAndReceiveMessage() throws Exception {
        long initialCount = receiver2.getLatch().getCount();
        this.mockMvcTester
                .post()
                .uri("/messages")
                .content(this.objectMapper.writeValueAsString(new MessageDTO("test_1", "junitTest")))
                .contentType(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk();

        // 4 from topic1 and 3 from topic2 on startUp, plus 1 from test
        await().pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> assertThat(receiver2.getLatch().getCount()).isEqualTo(initialCount - 1));
        assertThat(receiver2.getDeadLetterLatch().getCount()).isEqualTo(1);
    }

    @Test
    @Order(102)
    void sendAndReceiveMessageInDeadLetter() throws Exception {
        this.mockMvcTester
                .post()
                .uri("/messages")
                .content(this.objectMapper.writeValueAsString(new MessageDTO("test_1", "")))
                .contentType(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk();

        await().pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() ->
                        assertThat(receiver2.getDeadLetterLatch().getCount()).isZero());
    }

    @Test
    @Order(51)
    void topicsWithPartitionsCount() {
        String expectedJson =
                """
                [
                	{
                		"topicName": "__consumer_offsets",
                		"partitionCount": 1,
                		"replicationCount": 1
                	},
                	{
                		"topicName": "test_1",
                		"partitionCount": 32,
                		"replicationCount": 1
                	},
                	{
                		"topicName": "test_2",
                		"partitionCount": 1,
                		"replicationCount": 1
                	},
                	{
                		"topicName": "test_2-dlt",
                		"partitionCount": 1,
                		"replicationCount": 1
                	},
                	{
                		"topicName": "test_2-retry",
                		"partitionCount": 1,
                		"replicationCount": 1
                	},
                	{
                		"topicName": "test_3",
                		"partitionCount": 32,
                		"replicationCount": 1
                	}
                ]
                """;
        this.mockMvcTester
                .get()
                .uri("/topics")
                .param("showInternalTopics", "true")
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .isEqualTo(expectedJson);
    }

    @Test
    @Order(1)
    void getListOfContainers() {
        String expectedJson =
                """
                {
                    "topic_2_Listener": true,
                    "topic_2_Listener-retry": true,
                    "topic_1_Listener": true,
                    "topic_2_Listener-dlt": true
                }
                """;
        this.mockMvcTester
                .get()
                .uri("/listeners")
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .isEqualTo(expectedJson);
    }

    @Test
    @Order(2)
    void stopAndStartContainers() throws Exception {
        String expectedJson =
                """
                {
                        "topic_2_Listener": true,
                        "topic_2_Listener-retry": true,
                        "topic_1_Listener": true,
                        "topic_2_Listener-dlt": %s
                }
                """;
        this.mockMvcTester
                .post()
                .uri("/listeners")
                .content(this.objectMapper.writeValueAsString(
                        new KafkaListenerRequest("topic_2_Listener-dlt", Operation.STOP)))
                .contentType(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .isEqualTo(expectedJson.formatted(false));
        this.mockMvcTester
                .post()
                .uri("/listeners")
                .content(this.objectMapper.writeValueAsString(
                        new KafkaListenerRequest("topic_2_Listener-dlt", Operation.START)))
                .contentType(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .isEqualTo(expectedJson.formatted(true));
    }

    @Test
    @Order(3)
    void invalidContainerOperation() throws Exception {
        this.mockMvcTester
                .post()
                .uri("/listeners")
                .content(objectMapper.writeValueAsString(
                        new KafkaListenerRequest("invalid-container-id", Operation.STOP)))
                .contentType(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(problemDetail -> {
                    assertThat(problemDetail).isNotNull();
                    assertThat(problemDetail.getDetail())
                            .isNotNull()
                            .isEqualTo("Listener container with ID 'invalid-container-id' not found");
                    assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
                    assertThat(problemDetail.getInstance())
                            .isNotNull()
                            .isInstanceOf(URI.class)
                            .isEqualTo(URI.create("/listeners"));
                    assertThat(problemDetail.getType())
                            .isNotNull()
                            .isInstanceOf(URI.class)
                            .isEqualTo(URI.create("about:blank"));
                    assertThat(problemDetail.getStatus()).isEqualTo(400);
                });
    }

    @Test
    @Order(4)
    void whenInvalidOperation_thenReturnsBadRequest() {
        String invalidRequest =
                """
            {
                "containerId": "topic_2_Listener-dlt",
                 "operation": "INVALID"
            }
            """;

        this.mockMvcTester
                .post()
                .uri("/listeners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(problemDetail -> {
                    assertThat(problemDetail).isNotNull();
                    assertThat(problemDetail.getDetail())
                            .isNotNull()
                            .isEqualTo("Invalid operation value. Allowed values are: START, STOP.");
                    assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
                    assertThat(problemDetail.getInstance())
                            .isNotNull()
                            .isInstanceOf(URI.class)
                            .isEqualTo(URI.create("/listeners"));
                    assertThat(problemDetail.getType())
                            .isNotNull()
                            .isInstanceOf(URI.class)
                            .isEqualTo(URI.create("about:blank"));
                    assertThat(problemDetail.getStatus()).isEqualTo(400);
                });
    }
}
