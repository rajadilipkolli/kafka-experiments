package com.example.springbootkafka.multi.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbootkafka.multi.domain.SimpleMessage;
import com.example.springbootkafka.multi.sender.Sender;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MessageRestController {

    private final Sender sender;

    @PostMapping("/messages/simple")
    public void sendSimpleMessage(@RequestBody SimpleMessage message) {
        sender.send(message.key(), message.value());
    }

    @PostMapping("/messages/json")
    public void sendJsonMessage(@RequestBody SimpleMessage message) {
        sender.send(message);
    }

}
