package com.example.springbootkafkaavro.service;

import com.example.springbootkafkaavro.model.Person;
import com.example.springbootkafkaavro.util.ApplicationConstants;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class KafkaProducer {

    private final KafkaTemplate<String, Person> kafkaTemplate;
    private final long sendTimeoutMs;

    /**
     * Creates a producer with a bounded wait for each send result.
     *
     * @param kafkaTemplate template used to publish person records
     * @param sendTimeoutMs maximum wait for the send future, in milliseconds, after submission
     */
    public KafkaProducer(
            KafkaTemplate<String, Person> kafkaTemplate,
            @Value("${app.producer.send-timeout-ms:12000}") long sendTimeoutMs) {
        this.kafkaTemplate = kafkaTemplate;
        this.sendTimeoutMs = sendTimeoutMs;
    }

    /**
     * Publishes a person to the persons topic, keyed by name, and waits for the send result. A
     * timeout stops waiting without canceling the send. Interruption restores the interrupt flag.
     *
     * @throws ResponseStatusException with HTTP 503 if submission or waiting fails, including
     *     interruption or expiration of the configured wait
     */
    public void sendMessage(Person person) {
        try {
            CompletableFuture<SendResult<String, Person>> future =
                    this.kafkaTemplate.send(
                            ApplicationConstants.PERSONS_TOPIC,
                            person.getName().toString(),
                            person);
            future.get(this.sendTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Kafka send interrupted", e);
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Kafka send failed", e.getCause());
        } catch (TimeoutException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Kafka send timed out", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Kafka error", e);
        }
    }
}
