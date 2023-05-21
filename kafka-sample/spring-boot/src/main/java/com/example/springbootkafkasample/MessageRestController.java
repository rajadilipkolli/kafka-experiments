package com.example.springbootkafkasample;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageRestController {

    private final Sender sender;

    public MessageRestController(Sender sender) {
        this.sender = sender;
    }

    @PostMapping("/messages")
    public void sendMessage(@RequestBody Message message) {
        sender.send(message.topic(), message.msg());
    }

}

record Message(
        String topic,
        String msg) {
}