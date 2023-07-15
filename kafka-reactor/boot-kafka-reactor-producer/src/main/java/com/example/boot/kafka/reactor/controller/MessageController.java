package com.example.boot.kafka.reactor.controller;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import com.example.boot.kafka.reactor.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
class MessageController {

    private final MessageService messageService;

    @PostMapping
    public void sendMessage(@RequestBody @Valid MessageDTO messageDTO) {
        log.debug("sending messageDTO: {}", messageDTO);
        messageService.sendMessage(messageDTO);
    }
}
