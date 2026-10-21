package com.example.springbootkafkaavro.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.example.springbootkafkaavro.model.Person;
import com.example.springbootkafkaavro.service.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(controllers = KafkaController.class)
class KafkaControllerTest {

    @Autowired private MockMvcTester mockMvcTester;

    @MockitoBean private KafkaProducer kafkaProducer;

    @Test
    void whenProducerFails_thenReturns503() {
        doThrow(
                        new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE, "Kafka broker unavailable"))
                .when(kafkaProducer)
                .sendMessage(any(Person.class));

        mockMvcTester
                .post()
                .uri("/person/1/publish")
                .param("name", "John")
                .param("age", "30")
                .assertThat()
                .hasStatus(503);
    }

    @Test
    void whenValidationFails_thenReturns400() {
        mockMvcTester
                .post()
                .uri("/person/1/publish")
                .param("name", "")
                .param("age", "-1")
                .assertThat()
                .hasStatus(400);
    }
}
