package com.example.springbootkafka.multi.sender;

import static com.example.springbootkafka.multi.util.AppConstants.TOPIC_TEST_1;
import static com.example.springbootkafka.multi.util.AppConstants.TOPIC_TEST_2;

import com.example.springbootkafka.multi.domain.SimpleMessage;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.RoutingKafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class Sender {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

    private final RoutingKafkaTemplate routingKafkaTemplate;
    private final Tracer tracer;
    private final ObservationRegistry observationRegistry;
    private final long sendTimeoutMs;

    /**
     * Creates a sender with tracing and a bounded wait for each send result.
     *
     * @param routingKafkaTemplate template used to route records to producers
     * @param tracer tracer supplying the current span
     * @param observationRegistry registry for send observations
     * @param sendTimeoutMs maximum wait for the send future, in milliseconds, after submission
     */
    public Sender(
            RoutingKafkaTemplate routingKafkaTemplate,
            Tracer tracer,
            ObservationRegistry observationRegistry,
            @Value("${app.producer.send-timeout-ms:12000}") long sendTimeoutMs) {
        this.routingKafkaTemplate = routingKafkaTemplate;
        this.tracer = tracer;
        this.observationRegistry = observationRegistry;
        this.sendTimeoutMs = sendTimeoutMs;
    }

    /**
     * Publishes text to the first test topic with the supplied key and waits for the send result.
     * A timeout stops waiting without canceling the send.
     *
     * @throws InterruptedException if waiting is interrupted; the interrupt flag is restored
     * @throws ExecutionException if the send future fails or the configured wait expires
     * @throws RuntimeException if tracing or send submission fails synchronously
     */
    public void send(Integer key, String msg) throws InterruptedException, ExecutionException, RuntimeException {
        LOGGER.info("Sending key= {}, msg= {} to topic: {}", key, msg, TOPIC_TEST_1);
        try {
            CompletableFuture<SendResult<Object, Object>> future = Observation.createNotStarted(
                            "kafka-producer", this.observationRegistry)
                    .observeChecked(() -> {
                        LOGGER.info(
                                "<TRACE:{}> from producer for topic :{} ",
                                this.tracer.currentSpan().context().traceId(),
                                TOPIC_TEST_2);
                        CompletableFuture<SendResult<Object, Object>> sendFuture =
                                routingKafkaTemplate.send(TOPIC_TEST_1, key, msg);
                        return sendFuture.handle((result, throwable) -> {
                            LOGGER.info("Result <{}>, throwable <{}>", result, throwable);
                            if (throwable != null) {
                                if (throwable instanceof RuntimeException re) {
                                    throw re;
                                }
                                throw new CompletionException(throwable);
                            }
                            return result;
                        });
                    });
            future.get(this.sendTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (TimeoutException e) {
            throw new ExecutionException("Kafka send timed out", e);
        }
    }

    /**
     * Publishes a structured message to the second test topic using the current trace ID as its key
     * and waits for the send result. A timeout stops waiting without canceling the send.
     *
     * @throws InterruptedException if waiting is interrupted; the interrupt flag is restored
     * @throws ExecutionException if the send future fails or the configured wait expires
     * @throws RuntimeException if tracing or send submission fails synchronously
     */
    public void send(SimpleMessage simpleMessage) throws InterruptedException, ExecutionException, RuntimeException {
        try {
            CompletableFuture<SendResult<Object, Object>> future = Observation.createNotStarted(
                            "kafka-producer", this.observationRegistry)
                    .observeChecked(() -> {
                        String traceId = this.tracer.currentSpan().context().traceId();
                        LOGGER.info(
                                "Sending simpleMessage= {} with key= {}, to topic: {}",
                                simpleMessage,
                                traceId,
                                TOPIC_TEST_2);
                        Message<SimpleMessage> message = MessageBuilder.withPayload(simpleMessage)
                                .setHeader(KafkaHeaders.TOPIC, TOPIC_TEST_2)
                                .setHeader(KafkaHeaders.KEY, traceId)
                                .build();
                        CompletableFuture<SendResult<Object, Object>> sendFuture = routingKafkaTemplate.send(message);
                        return sendFuture.handle((result, throwable) -> {
                            LOGGER.info("Result <{}>, throwable <{}>", result, throwable);
                            if (throwable != null) {
                                if (throwable instanceof RuntimeException re) {
                                    throw re;
                                }
                                throw new CompletionException(throwable);
                            }
                            return result;
                        });
                    });
            future.get(this.sendTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (TimeoutException e) {
            throw new ExecutionException("Kafka send timed out", e);
        }
    }
}
