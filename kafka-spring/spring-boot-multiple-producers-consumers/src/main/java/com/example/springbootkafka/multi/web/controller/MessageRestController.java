package com.example.springbootkafka.multi.web.controller;

import com.example.springbootkafka.multi.domain.SimpleMessage;
import com.example.springbootkafka.multi.sender.Sender;
import io.micrometer.core.annotation.Timed;
import java.util.concurrent.ExecutionException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageRestController {

    private final Sender sender;

    public MessageRestController(Sender sender) {
        this.sender = sender;
    }

    @PostMapping("/messages/simple")
    public void sendSimpleMessage(@RequestBody SimpleMessage message)
            throws InterruptedException, ExecutionException, RuntimeException {
        sender.send(message.id(), message.value());
    }

    @PostMapping("/messages/json")
    @Timed
    public void sendJsonMessage(@RequestBody SimpleMessage message)
            throws InterruptedException, ExecutionException, RuntimeException {
        sender.send(message);
    }
}
