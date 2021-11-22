package com.sivalabs.sample;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = KafkaConfig.class)
@EmbeddedKafka(
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpringKafkaDemoTest {

    @Autowired
    MessageSender messageSender;

    @Autowired
    MessageListener messageListener;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;

    @BeforeAll
    public void setUp() {
        // wait until the partitions are assigned
        for (MessageListenerContainer messageListenerContainer :
                kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(
                    messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    void sendMessage() throws InterruptedException {
        messageSender.send("test-key","test-value");
        messageListener.getLatch().await(10, TimeUnit.SECONDS);
        assertThat(messageListener.getLatch().getCount()).isEqualTo(0);
    }
}