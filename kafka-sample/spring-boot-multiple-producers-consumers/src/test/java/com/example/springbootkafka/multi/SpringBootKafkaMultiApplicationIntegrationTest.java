package com.example.springbootkafka.multi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import com.example.springbootkafka.multi.domain.SimpleMessage;
import com.example.springbootkafka.multi.receiver.JsonReceiver;
import com.example.springbootkafka.multi.receiver.SimpleReceiver;
import com.example.springbootkafka.multi.sender.Sender;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestSpringBootKafkaMultiApplication.class)
class SpringBootKafkaMultiApplicationIntegrationTest {

    @Autowired
    private Sender sender;

    @Autowired
    private SimpleReceiver simpleReceiver;

    @Autowired
    private JsonReceiver jsonReceiver;

    @Test
    void sendAndReceiveData() throws Exception {
        sender.send(10, "foo");
        await().pollDelay(1, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS).untilAsserted(() -> assertThat(
                        simpleReceiver.getLatch().getCount())
                .isZero());
    }

    @Test
    void sendAndReceiveJsonData() throws Exception {
        SimpleMessage simpleMessage = new SimpleMessage(110, "My Json Message");
        sender.send(simpleMessage);
        await().pollDelay(1, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS).untilAsserted(() -> assertThat(
                        jsonReceiver.getLatch().getCount())
                .isZero());
    }
}
