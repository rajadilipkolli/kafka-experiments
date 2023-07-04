package com.example.springbootkafkasample.controller;

import com.example.springbootkafkasample.dto.MessageDTO;
import com.example.springbootkafkasample.sender.Sender;
import com.example.springbootkafkasample.service.MessageService;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageRestController {

    private final Sender sender;
    private final MessageService messageService;

    public MessageRestController(Sender sender, MessageService messageService) {
        this.sender = sender;
        this.messageService = messageService;
    }

    @PostMapping("/messages")
    public void sendMessage(@RequestBody MessageDTO messageDTO) {
        this.sender.send(messageDTO);
    }

    @GetMapping("/topics")
    public Map<String, Integer> getTopicsWithPartitionsCount(
            @RequestParam(name = "showInternalTopics", required = false, defaultValue = "false")
                    boolean showInternalTopics)
            throws ExecutionException, InterruptedException, TimeoutException {
        return messageService.getTopicsWithPartitions(showInternalTopics);
    }
}
