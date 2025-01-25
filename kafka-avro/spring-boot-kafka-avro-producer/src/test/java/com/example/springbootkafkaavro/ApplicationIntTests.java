package com.example.springbootkafkaavro;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.springbootkafkaavro.containers.KafkaContainersConfig;
import java.util.concurrent.TimeUnit;
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
        await().pollInterval(100, TimeUnit.MILLISECONDS)
                .atMost(10, SECONDS)
                .untilAsserted(
                        () ->
                                assertThat(output.getOut())
                                        .as("Should contain person details without gender")
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
                .exchange()
                .assertThat()
                .hasStatusOk();
        await().pollInterval(100, TimeUnit.MILLISECONDS)
                .atMost(10, SECONDS)
                .untilAsserted(
                        () ->
                                assertThat(output.getOut())
                                        .as("Should contain person details with gender")
                                        .contains("Person received : junit : 33 : male"));
    }

    @Test
    void concurrentPublishing(CapturedOutput output) throws Exception {
        int numberOfRequests = 10;
        for (int i = 0; i < numberOfRequests; i++) {
            this.mockMvcTester
                    .post()
                    .uri("/person/publish")
                    .param("name", "user" + i)
                    .param("age", String.valueOf(20 + i))
                    .exchange()
                    .assertThat()
                    .hasStatusOk();
        }
        await().pollInterval(100, TimeUnit.MILLISECONDS)
                .atMost(30, SECONDS)
                .untilAsserted(
                        () -> {
                            for (int i = 0; i < numberOfRequests; i++) {
                                assertThat(output.getOut())
                                        .contains("Person received : user" + i + " : " + (20 + i));
                            }
                        });
    }

    @Test
    void publishPersonWithoutName() {
        this.mockMvcTester
                .post()
                .uri("/person/publish")
                .param("age", "33")
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(
                        problemDetail ->
                                assertBadRequestProblem(
                                        problemDetail,
                                        "Required parameter 'name' is not present."));
    }

    @Test
    void publishPersonWithoutAge() {
        this.mockMvcTester
                .post()
                .uri("/person/publish")
                .param("name", "junit")
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(
                        problemDetail ->
                                assertBadRequestProblem(
                                        problemDetail, "Required parameter 'age' is not present."));
    }

    @Test
    void publishPersonWithEmptyName() {
        this.mockMvcTester
                .post()
                .uri("/person/publish")
                .param("name", "")
                .param("age", "33")
                .exchange()
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
                .exchange()
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

    private void assertBadRequestProblem(ProblemDetail problemDetail, String expectedDetail) {
        assertThat(problemDetail.getStatus()).isEqualTo(400);
        assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
        assertThat(problemDetail.getDetail()).contains(expectedDetail);
    }
}
