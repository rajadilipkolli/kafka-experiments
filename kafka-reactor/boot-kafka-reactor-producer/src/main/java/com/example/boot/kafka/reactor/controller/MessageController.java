package com.example.boot.kafka.reactor.controller;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import com.example.boot.kafka.reactor.service.MessageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public Mono<Void> sendMessage(@RequestBody @Valid MessageDTO messageDTO) {
        log.debug("sending messageDTO: {}", messageDTO);
        return messageService.sendMessage(messageDTO).onErrorResume(ex -> {
            log.error("Failed to send message: {}", ex.getMessage());
            return Mono.error(new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Kafka broker unavailable: " + ex.getMessage(), ex));
        });
    }

    @ExceptionHandler(ResponseStatusException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    ProblemDetail handleResponseStatusException(ResponseStatusException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getReason());
        problemDetail.setTitle("Service Unavailable");
        return problemDetail;
    }
}
