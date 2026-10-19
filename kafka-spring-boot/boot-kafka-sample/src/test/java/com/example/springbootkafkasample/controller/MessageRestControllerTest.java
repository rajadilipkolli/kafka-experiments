package com.example.springbootkafkasample.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.example.springbootkafkasample.dto.MessageDTO;
import com.example.springbootkafkasample.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.KafkaException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = MessageRestController.class)
class MessageRestControllerTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessageService messageService;

    @Test
    void whenSendFails_thenReturns503() {
        doThrow(new KafkaException("Kafka broker unavailable"))
                .when(messageService)
                .send(any(MessageDTO.class));

        MessageDTO dto = new MessageDTO("test", "hello");

        mockMvcTester
                .post()
                .uri("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .assertThat()
                .hasStatus(503);
    }
}
