package com.example.springbootkafkaavro;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        properties = {
            "spring.kafka.consumer.group-id=group-1",
            "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
            "spring.kafka.consumer.value-deserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer",
            "spring.kafka.properties.specific.avro.reader=true"
        },
        classes = TestSpringBootKafkaAvroProducerApplication.class)
@AutoConfigureMockMvc
@Import(AvroKafkaListener.class)
@ExtendWith(OutputCaptureExtension.class)
class SpringBootKafkaAvroProducerApplicationIntTests {

    @Autowired MockMvc mockMvc;

    @Test
    void contextLoads(CapturedOutput output) throws Exception {
        this.mockMvc
                .perform(post("/person/publish").param("name", "junit").param("age", "33"))
                .andExpect(status().isOk());
        await().atMost(30, SECONDS)
                .untilAsserted(
                        () -> assertThat(output.getOut()).contains("Person received : junit : 33"));
    }
}
