package com.example.boot.kafka.reactor.controller;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import com.example.boot.kafka.reactor.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
class MessageController {

    private final MessageService messageService;

    @PostMapping
    public Mono<ResponseEntity<Object>> sendMessage(@RequestBody @Valid MessageDTO messageDTO) {
        log.debug("sending messageDTO: {}", messageDTO);
        return messageService.sendMessage(messageDTO);
    }
}
