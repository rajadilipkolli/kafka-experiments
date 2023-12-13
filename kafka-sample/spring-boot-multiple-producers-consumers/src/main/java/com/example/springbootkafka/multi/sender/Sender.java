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
import org.springframework.kafka.support.SendResult;
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

    public void send(SimpleMessage msg) throws InterruptedException, ExecutionException, RuntimeException {
        log.info("Sending key= {}, msg= {} to topic: {}", msg.key(), msg, TOPIC_TEST_2);
        Observation.createNotStarted("kafka-producer", this.observationRegistry)
                .observeChecked(() -> {
                    log.info(
                            "<TRACE:{}> from producer for topic :{} ",
                            this.tracer.currentSpan().context().traceId(),
                            TOPIC_TEST_2);
                    CompletableFuture<SendResult<Object, Object>> future =
                            routingKafkaTemplate.send(TOPIC_TEST_2, String.valueOf(msg.key()), msg);
                    return future.handle((result, throwable) -> {
                        log.info("Result <{}>, throwable <{}>", result, throwable);
                        return CompletableFuture.completedFuture(result);
                    });
                })
                .get();
    }
}
