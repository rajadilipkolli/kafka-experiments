package com.example.springbootkafka.multi;

import static com.example.springbootkafka.multi.util.AppConstants.TOPIC_TEST_1;
import static com.example.springbootkafka.multi.util.AppConstants.TOPIC_TEST_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.springbootkafka.multi.domain.SimpleMessage;
import com.example.springbootkafka.multi.receiver.JsonReceiver;
import com.example.springbootkafka.multi.receiver.SimpleReceiver;
import com.example.springbootkafka.multi.sender.Sender;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@EmbeddedKafka(topics = {TOPIC_TEST_1, TOPIC_TEST_2})
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpringBootKafkaMultiApplicationTests {

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private Sender sender;

    @Autowired
    private SimpleReceiver simpleReceiver;

    @Autowired
    private JsonReceiver jsonReceiver;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @BeforeAll
    void setUp() {
        // wait until the partitions are assigned
        for (MessageListenerContainer messageListenerContainer :
                kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    void sendAndReceiveData() throws Exception {
        sender.send(10, "foo");
        await().pollDelay(1, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(
                        simpleReceiver.getLatch().getCount())
                .isZero());
    }

    @Test
    void sendAndReceiveJsonData() throws Exception {
        SimpleMessage simpleMessage = new SimpleMessage(110, "My Json Message");
        sender.send(simpleMessage);
        await().pollDelay(1, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(
                        jsonReceiver.getLatch().getCount())
                .isZero());
    }
}
