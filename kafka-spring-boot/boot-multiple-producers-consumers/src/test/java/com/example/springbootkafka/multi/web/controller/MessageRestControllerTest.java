package com.example.springbootkafka.multi.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

import com.example.springbootkafka.multi.domain.SimpleMessage;
import com.example.springbootkafka.multi.sender.Sender;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
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
    private Sender sender;

    @Test
    void whenSendSimpleFails_thenReturns503() throws Exception {
        doThrow(new ExecutionException("Broker timeout", new RuntimeException()))
                .when(sender)
                .send(anyInt(), anyString());

        SimpleMessage message = new SimpleMessage(1, "test");

        mockMvcTester
                .post()
                .uri("/messages/simple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(message))
                .assertThat()
                .hasStatus(503);
    }

    @Test
    void whenSendJsonFails_thenReturns503() throws Exception {
        doThrow(new ExecutionException("Broker timeout", new RuntimeException()))
                .when(sender)
                .send(any(SimpleMessage.class));

        SimpleMessage message = new SimpleMessage(1, "test");

        mockMvcTester
                .post()
                .uri("/messages/json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(message))
                .assertThat()
                .hasStatus(503);
    }
}
