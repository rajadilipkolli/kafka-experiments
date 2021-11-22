package com.sivalabs.springbootkafka.multi;

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

import java.util.concurrent.TimeUnit;

import static com.sivalabs.springbootkafka.multi.SpringBootKafkaMultiApplication.TOPIC_TEST_1;
import static com.sivalabs.springbootkafka.multi.SpringBootKafkaMultiApplication.TOPIC_TEST_2;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(
    topics = {TOPIC_TEST_1, TOPIC_TEST_2},
    brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SpringBootKafkaMultiApplicationTests {

  @Autowired private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

  @Autowired private KafkaTemplate<Integer, String> simpleKafkaTemplate;

  @Autowired private KafkaTemplate<String, SimpleMessage> jsonKafkaTemplate;

  @Autowired private Sender sender;

  @Autowired SimpleReceiver simpleReceiver;

  @Autowired JsonReceiver jsonReceiver;

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
  void sendAndReceiveData() throws Exception {
    // simpleKafkaTemplate.send(TOPIC_TEST_1, 10,"foo");
    sender.send(10, "foo");
    // TimeUnit.SECONDS.sleep(5);
    simpleReceiver.getLatch().await(5, TimeUnit.SECONDS);
    assertThat(simpleReceiver.getLatch().getCount()).isEqualTo(0);
  }

  @Test
  void sendAndReceiveJsonData() throws Exception {
    SimpleMessage simpleMessage = new SimpleMessage();
    simpleMessage.setKey(110);
    simpleMessage.setValue("My Json Message");
    sender.send(simpleMessage);
    // jsonKafkaTemplate.send(TOPIC_TEST_2, "k1",simpleMessage);
    // TimeUnit.SECONDS.sleep(5);
    jsonReceiver.getLatch().await(5, TimeUnit.SECONDS);
    assertThat(jsonReceiver.getLatch().getCount()).isEqualTo(0);
  }
}
