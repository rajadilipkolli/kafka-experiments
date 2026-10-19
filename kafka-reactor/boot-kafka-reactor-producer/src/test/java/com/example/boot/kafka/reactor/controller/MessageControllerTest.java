package com.example.boot.kafka.reactor.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.example.boot.kafka.reactor.entity.MessageDTO;
import com.example.boot.kafka.reactor.service.MessageService;
import org.apache.kafka.common.errors.TimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = MessageController.class)
class MessageControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private MessageService messageService;

    @Test
    void whenMessageServiceFails_thenReturns503() {
        given(messageService.sendMessage(any(MessageDTO.class)))
                .willReturn(Mono.error(new TimeoutException("Kafka broker unavailable")));

        String requestBody =
                """
                {
                    "id": 1,
                    "text": "test",
                    "sentAt": "2023-06-15T18:49:38.813Z"
                }
                """;

        webTestClient
                .post()
                .uri("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isEqualTo(503);
    }
}
