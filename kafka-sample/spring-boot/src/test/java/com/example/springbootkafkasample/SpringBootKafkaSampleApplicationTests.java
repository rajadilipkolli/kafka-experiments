package com.example.springbootkafkasample;

import static com.example.springbootkafkasample.config.Initializer.TOPIC_TEST_1;
import static com.example.springbootkafkasample.config.Initializer.TOPIC_TEST_2;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.springbootkafkasample.dto.MessageDTO;
import com.example.springbootkafkasample.listener.Receiver2;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@EmbeddedKafka(
        topics = {TOPIC_TEST_1, TOPIC_TEST_2},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpringBootKafkaSampleApplicationTests {

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, MessageDTO> template;

    @Autowired
    private Receiver2 receiver2;

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
        template.send(TOPIC_TEST_1, UUID.randomUUID().toString(), new MessageDTO(TOPIC_TEST_1, "foo"));
        receiver2.getLatch().await(5, TimeUnit.SECONDS);
        assertThat(receiver2.getLatch().getCount()).isEqualTo(4);
    }
}
