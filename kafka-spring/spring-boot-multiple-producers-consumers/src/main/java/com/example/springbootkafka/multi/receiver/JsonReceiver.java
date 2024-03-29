package com.example.springbootkafka.multi.receiver;

import static com.example.springbootkafka.multi.util.AppConstants.TOPIC_TEST_2;

import com.example.springbootkafka.multi.domain.SimpleMessage;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import jakarta.validation.Valid;
import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Component
@Slf4j
@Validated
public class JsonReceiver {

    private final Tracer tracer;

    private final ObservationRegistry observationRegistry;

    JsonReceiver(Tracer tracer, ObservationRegistry observationRegistry) {
        this.tracer = tracer;
        this.observationRegistry = observationRegistry;
    }

    private final CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = TOPIC_TEST_2, containerFactory = "jsonKafkaListenerContainerFactory")
    public void jsonListener(
            @Payload @Valid SimpleMessage simpleMessage, @Header(KafkaHeaders.RECEIVED_KEY) String receivedKey) {

        Observation.createNotStarted("jsonListener", this.observationRegistry)
                .observe(() -> log.info(
                        "Received in jsonListener message {} with receivedKey :{}", simpleMessage, receivedKey));
        latch.countDown();
    }
}
