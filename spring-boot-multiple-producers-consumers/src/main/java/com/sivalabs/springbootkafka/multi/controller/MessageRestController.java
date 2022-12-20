package com.sivalabs.springbootkafka.multi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sivalabs.springbootkafka.multi.domain.SimpleMessage;
import com.sivalabs.springbootkafka.multi.sender.Sender;

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
