package com.example.springbootkafka.multi.receiver;

import static com.example.springbootkafka.multi.util.AppConstants.TOPIC_TEST_2;

import com.example.springbootkafka.multi.domain.SimpleMessage;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import jakarta.validation.Valid;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class JsonReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonReceiver.class);

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
                .observe(() -> LOGGER.info(
                        "Received in jsonListener message {} with receivedKey :{}", simpleMessage, receivedKey));
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}
