package com.example.springbootkafkasample;

import static com.example.springbootkafkasample.config.Initializer.TOPIC_TEST_1;
import static com.example.springbootkafkasample.config.Initializer.TOPIC_TEST_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.springbootkafkasample.dto.MessageDTO;
import com.example.springbootkafkasample.service.listener.Receiver2;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@EmbeddedKafka(
        topics = {TOPIC_TEST_1, TOPIC_TEST_2},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
class KafkaSampleApplicationTests {

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<UUID, MessageDTO> template;

    @Autowired
    private Receiver2 receiver2;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public void setUp() {
        // wait until the partitions are assigned
        for (MessageListenerContainer messageListenerContainer :
                kafkaListenerEndpointRegistry.getListenerContainers()) {
            // By default embeddedkafka assigns 2 partitions and if we let kakfa to create dlt and retry topics default
            // partitions is 1
            String groupId = messageListenerContainer.getContainerProperties().getGroupId();
            if ("foo-dlt".equals(groupId) || groupId.contains("foo-retry")) {
                ContainerTestUtils.waitForAssignment(messageListenerContainer, 1);
            } else {
                ContainerTestUtils.waitForAssignment(
                        messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
            }
        }
    }

    @Test
    void sendAndReceiveData() throws InterruptedException {
        template.send(TOPIC_TEST_1, UUID.randomUUID(), new MessageDTO(TOPIC_TEST_1, "foo"));
        // 4 from topic1 and 3 from topic2 on startUp, plus 1 from test
        await().pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> assertThat(receiver2.getLatch().getCount()).isEqualTo(9));
    }

    @Test
    void testTopicsWithPartitionsCount() throws Exception {

        String expectedJson =
                """
                [
                    {"topicName":"test_2-dlt","partitionCount":1,"replicationCount":1},
                    {"topicName":"test_3","partitionCount":1,"replicationCount":1},
                    {"topicName":"test_2","partitionCount":2,"replicationCount":1},
                    {"topicName":"test_1","partitionCount":2,"replicationCount":1},
                    {"topicName":"test_2-retry-0","partitionCount":1,"replicationCount":1},
                    {"topicName":"test_2-retry-1","partitionCount":1,"replicationCount":1}
                ]
                """;
        this.mockMvc
                .perform(get("/topics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }
}
