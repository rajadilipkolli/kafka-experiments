package com.example.springbootkafkaavro;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.springbootkafkaavro.containers.KafkaContainersConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest(
        properties = {
            "spring.kafka.consumer.group-id=group-1",
            "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
            "spring.kafka.consumer.value-deserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer",
            "spring.kafka.properties.specific.avro.reader=true"
        },
        classes = {KafkaContainersConfig.class})
@AutoConfigureMockMvc
@Import(AvroKafkaListener.class)
@ExtendWith(OutputCaptureExtension.class)
class ApplicationIntTests {

    @Autowired MockMvcTester mockMvcTester;

    @Test
    void publishPersonWithoutGender(CapturedOutput output) {
        this.mockMvcTester
                .post()
                .uri("/person/publish")
                .param("name", "junit")
                .param("age", "33")
                .exchange()
                .assertThat()
                .hasStatusOk();
        await().atMost(10, SECONDS)
                .untilAsserted(
                        () ->
                                assertThat(output.getOut())
                                        .contains("Person received : junit : 33 : "));
    }

    @Test
    void publishPersonWithGender(CapturedOutput output) {
        this.mockMvcTester
                .post()
                .uri("/person/publish")
                .param("name", "junit")
                .param("age", "33")
                .param("gender", "male")
                .assertThat()
                .hasStatusOk();
        await().atMost(10, SECONDS)
                .untilAsserted(
                        () ->
                                assertThat(output.getOut())
                                        .contains("Person received : junit : 33 : male"));
    }

    @Test
    void publishPersonWithoutName() {
        this.mockMvcTester
                .post()
                .uri("/person/publish")
                .param("age", "33")
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(
                        problemDetail -> {
                            assertThat(problemDetail.getStatus()).isEqualTo(400);
                            assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
                            assertThat(problemDetail.getDetail())
                                    .contains("Required parameter 'name' is not present.");
                        });
    }

    @Test
    void publishPersonWithoutAge() {
        this.mockMvcTester
                .post()
                .uri("/person/publish")
                .param("name", "junit")
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(
                        problemDetail -> {
                            assertThat(problemDetail.getStatus()).isEqualTo(400);
                            assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
                            assertThat(problemDetail.getDetail())
                                    .contains("Required parameter 'age' is not present.");
                        });
    }

    @Test
    void publishPersonWithEmptyName() {
        this.mockMvcTester
                .post()
                .uri("/person/publish")
                .param("name", "")
                .param("age", "33")
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(
                        problemDetail -> {
                            assertThat(problemDetail.getStatus()).isEqualTo(400);
                            assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
                            assertThat(problemDetail.getDetail()).contains("Validation failure");
                        });
    }

    @Test
    void publishPersonWithNegativeAge() {
        this.mockMvcTester
                .post()
                .uri("/person/publish")
                .param("name", "junit")
                .param("age", "-1")
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(
                        problemDetail -> {
                            assertThat(problemDetail.getStatus()).isEqualTo(400);
                            assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
                            assertThat(problemDetail.getDetail()).contains("Validation failure");
                        });
    }
}
