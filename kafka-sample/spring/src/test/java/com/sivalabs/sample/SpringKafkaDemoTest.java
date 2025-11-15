package com.sivalabs.sample;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = KafkaConfig.class)
@EmbeddedKafka(
        partitions = 1,
        topics = {KafkaConfig.TOPIC})
@DirtiesContext
class SpringKafkaDemoTest {

    @Autowired
    MessageSender messageSender;

    @Autowired
    MessageListener messageListener;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @BeforeEach
    public void setUp() {

        // wait until the partitions are assigned
        for (MessageListenerContainer messageListenerContainer :
                kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    void sendMessage() throws InterruptedException {
        messageSender.send("test-key", "test-value");
        boolean messageReceived = messageListener.getLatch().await(10, TimeUnit.SECONDS);
        assertThat(messageReceived).isTrue();
        assertThat(messageListener.getLatch().getCount()).isEqualTo(0);
    }
}
