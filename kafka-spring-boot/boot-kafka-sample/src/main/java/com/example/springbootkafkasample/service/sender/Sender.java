package com.example.springbootkafkasample.service.sender;

import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

import com.example.springbootkafkasample.dto.MessageDTO;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
public class Sender {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

    private final KafkaTemplate<UUID, MessageDTO> template;
    private final long sendTimeoutMs;

    /**
     * Creates a sender with a bounded wait for each send result.
     *
     * @param template template used to publish messages
     * @param sendTimeoutMs maximum wait for the send future, in milliseconds, after submission
     */
    public Sender(
            KafkaTemplate<UUID, MessageDTO> template,
            @Value("${app.producer.send-timeout-ms:12000}") long sendTimeoutMs) {
        this.template = template;
        this.sendTimeoutMs = sendTimeoutMs;
    }

    /**
     * Sends to the message's topic with a random UUID key and waits for the send result.
     * A timeout stops waiting without canceling the send.
     *
     * @throws KafkaException if waiting is interrupted, times out, or fails with a non-runtime
     *     cause; interruption also restores the interrupt flag
     * @throws RuntimeException if submission fails or the send future fails with a runtime cause
     */
    @AsyncPublisher(
            operation =
                    @AsyncOperation(
                            channelName = "test1",
                            description = "MessageDTO payload",
                            headers =
                                    @AsyncOperation.Headers(
                                            schemaName = "SpringKafkaDefaultHeaders",
                                            values = {
                                                @AsyncOperation.Headers.Header(
                                                        name = DEFAULT_CLASSID_FIELD_NAME,
                                                        description = "Spring Type Id Header",
                                                        value = "com.example.springbootkafkasample.dto.MessageDTO"),
                                            })))
    @KafkaAsyncOperationBinding
    public void send(MessageDTO messageDTO) {
        CompletableFuture<SendResult<UUID, MessageDTO>> future =
                this.template.send(messageDTO.topic(), UUID.randomUUID(), messageDTO);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                LOGGER.info(
                        "Sent message=[{}] with offset=[{}]",
                        messageDTO,
                        result.getRecordMetadata().offset());
            } else {
                LOGGER.warn("Unable to send message=[{}] due to : {}", messageDTO, ex.getMessage());
            }
        });
        try {
            future.get(this.sendTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KafkaException("Interrupted while waiting for Kafka send", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new KafkaException("Failed to send message to Kafka", cause);
        } catch (TimeoutException e) {
            throw new KafkaException("Timed out waiting for Kafka send", e);
        }
    }
}
