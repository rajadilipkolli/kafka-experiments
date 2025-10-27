package com.example.springbootkafka.multi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.springbootkafka.multi.common.ContainerConfiguration;
import com.example.springbootkafka.multi.domain.SimpleMessage;
import com.example.springbootkafka.multi.receiver.JsonReceiver;
import com.example.springbootkafka.multi.receiver.SimpleReceiver;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ContainerConfiguration.class)
@AutoConfigureMockMvc
class SpringBootKafkaMultiApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SimpleReceiver simpleReceiver;

    @Autowired
    private JsonReceiver jsonReceiver;

    @Test
    void sendAndReceiveData() throws Exception {
        this.mockMvc
                .perform(post("/messages/simple")
                        .content(this.objectMapper.writeValueAsString(new SimpleMessage(10, "foo")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        await().pollDelay(1, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS).untilAsserted(() -> assertThat(
                        simpleReceiver.getLatch().getCount())
                .isZero());
    }

    @Test
    void sendAndReceiveJsonData() throws Exception {
        SimpleMessage simpleMessage = new SimpleMessage(110, "My Json Message");
        this.mockMvc
                .perform(post("/messages/json")
                        .content(this.objectMapper.writeValueAsString(simpleMessage))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        await().pollDelay(1, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS).untilAsserted(() -> assertThat(
                        jsonReceiver.getLatch().getCount())
                .isZero());
    }
}
