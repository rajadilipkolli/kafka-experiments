package com.example.springbootkafkasample.controller;

import com.example.springbootkafkasample.dto.MessageDTO;
import com.example.springbootkafkasample.sender.Sender;
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
    public void sendMessage(@RequestBody MessageDTO messageDTO) {
        this.sender.send(messageDTO);
    }

}

