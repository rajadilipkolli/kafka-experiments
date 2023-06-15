package com.example.boot.kafka.reactor;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.boot.kafka.reactor.config.MyTestContainers;
import com.example.boot.kafka.reactor.entity.MessageDTO;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.test.StepVerifier;

@Slf4j
@SpringBootTest(classes = {MyTestContainers.class})
@AutoConfigureWebTestClient
class BootKafkaReactorProducerApplicationTests {

    @Autowired
    KafkaReceiver<Integer, MessageDTO> receiver;

    @Autowired
    protected WebTestClient webTestClient;

    @Test
    void contextLoads() throws InterruptedException {
        assertThat(receiver).isNotNull();
    }

    @Test
    void loadDataAndConsume() throws InterruptedException {
        String requestBody =
                """
                {
                    "id": 10000,
                    "text": "hello1",
                    "sentAt": "2023-06-15T18:49:38.813Z"
                }
                    """;
        this.webTestClient
                .post()
                .uri("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(requestBody), String.class)
                .exchange()
                .expectStatus()
                .isCreated();

        TimeUnit.SECONDS.sleep(5);
        Flux<MessageDTO> flux = receiver.receive().map(record -> {
            ReceiverOffset offset = record.receiverOffset();
            var value = record.value();
            log.debug(
                    "Received message: topic-partition={} offset={} timestamp={} key={} value={}",
                    offset.topicPartition(),
                    offset.offset(),
                    LocalDateTime.now(),
                    record.key(),
                    value);
            offset.acknowledge();
            return value;
        });
        assertThat(flux).isNotNull();

        StepVerifier.create(flux).expectNextCount(1).verifyComplete();
    }
}
