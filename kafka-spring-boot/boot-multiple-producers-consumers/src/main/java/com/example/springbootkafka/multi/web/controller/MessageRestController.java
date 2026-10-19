package com.example.springbootkafka.multi.web.controller;

import com.example.springbootkafka.multi.domain.SimpleMessage;
import com.example.springbootkafka.multi.sender.Sender;
import io.micrometer.core.annotation.Timed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.KafkaException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @ExceptionHandler({
        ExecutionException.class,
        TimeoutException.class,
        InterruptedException.class,
        KafkaException.class,
        org.apache.kafka.common.KafkaException.class
    })
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<Void> handleSendFailure(Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
