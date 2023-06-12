package com.example.springbootkafkasample;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.springbootkafkasample.dto.MessageDTO;
import com.example.springbootkafkasample.listener.Receiver2;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@Import(TestSpringBootKafkaSampleApplication.class)
@AutoConfigureMockMvc
@DirtiesContext
class SpringBootKafkaSampleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Receiver2 receiver2;

    @Test
    void testSendAndReceiveMessage() throws Exception {
        this.mockMvc
                .perform(post("/messages")
                        .content(this.objectMapper.writeValueAsString(new MessageDTO("test_1", "junitTest")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        receiver2.getLatch().await(10, TimeUnit.SECONDS);
        assertThat(receiver2.getLatch().getCount()).isZero();
    }
}
