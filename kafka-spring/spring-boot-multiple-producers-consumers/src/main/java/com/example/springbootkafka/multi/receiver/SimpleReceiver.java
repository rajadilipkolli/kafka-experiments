package com.example.springbootkafka.multi.receiver;

import static com.example.springbootkafka.multi.util.AppConstants.TOPIC_TEST_1;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Getter
@Component
@Slf4j
public class SimpleReceiver {

    private final Tracer tracer;

    private final ObservationRegistry observationRegistry;

    SimpleReceiver(Tracer tracer, ObservationRegistry observationRegistry) {
        this.tracer = tracer;
        this.observationRegistry = observationRegistry;
    }

    private final CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = TOPIC_TEST_1, containerFactory = "simpleKafkaListenerContainerFactory")
    public void simpleListener(ConsumerRecord<Integer, String> cr) {

        Observation.createNotStarted("simpleListener", this.observationRegistry)
                .observe(() -> log.info(
                        "Received a message in simpleListener with key = {} , value={} with traceId : {}",
                        cr.key(),
                        cr.value(),
                        this.tracer.currentSpan().context().traceId()));
        latch.countDown();
    }
}
