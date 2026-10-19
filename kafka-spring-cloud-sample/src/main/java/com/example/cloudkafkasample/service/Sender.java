/* (C)2026 */
package com.example.cloudkafkasample.service;

import com.example.cloudkafkasample.domain.MessageDTO;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Sender {

    private final StreamBridge streamBridge;

    public Sender(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    /**
     * Sends a JSON message through the {@code receive-out-0} binding.
     *
     * @throws ResponseStatusException with HTTP 503 if the bridge rejects the send or raises
     *     a {@link MessagingException}
     * @throws RuntimeException if the bridge raises another runtime exception; it propagates unchanged
     */
    public void send(MessageDTO messageDTO) {
        boolean sent;
        try {
            sent = this.streamBridge.send("receive-out-0", messageDTO, MediaType.APPLICATION_JSON);
        } catch (MessagingException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Kafka send failed or broker unavailable", ex);
        }
        if (!sent) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Kafka send failed or broker unavailable");
        }
    }
}
