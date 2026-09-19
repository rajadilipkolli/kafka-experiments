/* (C)2023 */
package com.example.cloudkafkasample.service;

import com.example.cloudkafkasample.domain.MessageDTO;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class Sender {

    private final StreamBridge streamBridge;

    public Sender(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void send(MessageDTO messageDTO) {
        this.streamBridge.send("receive-out-0", messageDTO, MediaType.APPLICATION_JSON);
    }
}
