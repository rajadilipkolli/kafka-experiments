/* (C)2023 */
package com.example.cloudkafkasample;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import com.example.cloudkafkasample.common.ContainersConfig;
import com.example.cloudkafkasample.sink.Receiver;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ContainersConfig.class)
@AutoConfigureMockMvc
class CloudKafkaSampleApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Receiver receiver;

    @Test
    void publishingAndSubscribing() throws Exception {
        this.mockMvc
                .perform(
                        post("/messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "msg": "string"
                                }
                                """))
                .andExpect(status().isOk());
        await().pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(receiver.getLatch().getCount()).isZero());
    }
}
