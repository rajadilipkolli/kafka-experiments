package com.example.boot.kafka.reactor.controller;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import com.example.boot.kafka.reactor.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
class MessageController {

    private final MessageService messageService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MessageDTO> events() {
        return messageService.fetchMessages();
    }
}
