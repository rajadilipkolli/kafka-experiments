package com.example.springbootkafka.multi.sender;

import static com.example.springbootkafka.multi.util.AppConstants.TOPIC_TEST_1;
import static com.example.springbootkafka.multi.util.AppConstants.TOPIC_TEST_2;

import com.example.springbootkafka.multi.domain.SimpleMessage;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.RoutingKafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class Sender {

    private final RoutingKafkaTemplate routingKafkaTemplate;
    private final Tracer tracer;
    private final ObservationRegistry observationRegistry;

    public void send(Integer key, String msg) throws InterruptedException, ExecutionException, RuntimeException {
        log.info("Sending key= {}, msg= {} to topic: {}", key, msg, TOPIC_TEST_1);
        Observation.createNotStarted("kafka-producer", this.observationRegistry)
                .observeChecked(() -> {
                    log.info(
                            "<TRACE:{}> from producer for topic :{} ",
                            this.tracer.currentSpan().context().traceId(),
                            TOPIC_TEST_2);
                    CompletableFuture<SendResult<Object, Object>> future =
                            routingKafkaTemplate.send(TOPIC_TEST_1, key, msg);
                    return future.handle((result, throwable) -> {
                        log.info("Result <{}>, throwable <{}>", result, throwable);
                        return CompletableFuture.completedFuture(result);
                    });
                })
                .get();
    }

    public void send(SimpleMessage simpleMessage) throws InterruptedException, ExecutionException, RuntimeException {
        Observation.createNotStarted("kafka-producer", this.observationRegistry)
                .observeChecked(() -> {
                    String traceId = this.tracer.currentSpan().context().traceId();
                    log.info(
                            "Sending simpleMessage= {} with key= {}, to topic: {}",
                            simpleMessage,
                            traceId,
                            TOPIC_TEST_2);
                    // Using MessageBuilder to create a message with headers and payload
                    Message<SimpleMessage> message = MessageBuilder.withPayload(simpleMessage)
                            .setHeader(KafkaHeaders.TOPIC, TOPIC_TEST_2)
                            .setHeader(KafkaHeaders.KEY, traceId)
                            .build();
                    CompletableFuture<SendResult<Object, Object>> future = routingKafkaTemplate.send(message);
                    return future.handle((result, throwable) -> {
                        log.info("Result <{}>, throwable <{}>", result, throwable);
                        return CompletableFuture.completedFuture(result);
                    });
                })
                .get();
    }
}
