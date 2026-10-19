/* (C)2026 */
package com.example.cloudkafkasample.service;

import com.example.cloudkafkasample.domain.MessageDTO;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Sender {

    private final StreamBridge streamBridge;

    public Sender(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void send(MessageDTO messageDTO) {
        boolean sent = this.streamBridge.send("receive-out-0", messageDTO, MediaType.APPLICATION_JSON);
        if (!sent) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Kafka send failed or broker unavailable");
        }
    }
}
