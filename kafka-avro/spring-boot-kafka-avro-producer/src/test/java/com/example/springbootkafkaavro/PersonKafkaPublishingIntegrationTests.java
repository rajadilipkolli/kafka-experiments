package com.example.springbootkafkaavro;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.springbootkafkaavro.containers.KafkaContainersConfig;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest(classes = {KafkaContainersConfig.class})
@AutoConfigureMockMvc
@Import(AvroKafkaListener.class)
@ExtendWith(OutputCaptureExtension.class)
@ActiveProfiles("test")
class PersonKafkaPublishingIntegrationTests {

    @Autowired MockMvcTester mockMvcTester;

    @Test
    void publishPersonWithoutGender(CapturedOutput output) {
        this.mockMvcTester
                .post()
                .uri("/person/v1/publish")
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
                .uri("/person/v1/publish")
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
    void concurrentPublishing(CapturedOutput output) {
        int numberOfRequests = 10;
        for (int i = 0; i < numberOfRequests; i++) {
            this.mockMvcTester
                    .post()
                    .uri("/person/v1/publish")
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
                .uri("/person/v1/publish")
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
                .uri("/person/v1/publish")
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
                .uri("/person/v1/publish")
                .param("name", "")
                .param("age", "33")
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(this::assertBadRequestProblem);
    }

    @Test
    void publishPersonWithNegativeAge() {
        this.mockMvcTester
                .post()
                .uri("/person/v1/publish")
                .param("name", "junit")
                .param("age", "-1")
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(this::assertBadRequestProblem);
    }

    @ParameterizedTest
    @MethodSource("v2PersonTestParameters")
    void publishV2PersonVariations(
            String testName,
            String name,
            String age,
            String gender,
            String email,
            String phoneNumber,
            String expectedBasicOutput,
            String expectedV2Output,
            boolean shouldContainV2Output,
            CapturedOutput output) {

        var requestBuilder =
                this.mockMvcTester
                        .post()
                        .uri("/person/v2/publish")
                        .param("name", name)
                        .param("age", age);

        if (gender != null) {
            requestBuilder.param("gender", gender);
        }
        if (email != null) {
            requestBuilder.param("email", email);
        }
        if (phoneNumber != null) {
            requestBuilder.param("phoneNumber", phoneNumber);
        }

        requestBuilder.exchange().assertThat().hasStatusOk();

        await().pollInterval(100, TimeUnit.MILLISECONDS)
                .atMost(10, SECONDS)
                .untilAsserted(
                        () -> {
                            assertThat(output.getOut())
                                    .as("Should contain basic person details")
                                    .contains(expectedBasicOutput);

                            if (shouldContainV2Output) {
                                assertThat(output.getOut())
                                        .as("Should contain V2 fields")
                                        .contains(expectedV2Output);
                            } else {
                                assertThat(output.getOut())
                                        .as(
                                                "Should not contain V2 field logging for minimal fields")
                                        .doesNotContain("V2 Person details");
                            }
                        });
    }

    static Stream<Arguments> v2PersonTestParameters() {
        return Stream.of(
                Arguments.of(
                        "All fields",
                        "jane",
                        "25",
                        "female",
                        "jane@example.com",
                        "+1-555-0123",
                        "Person received : jane : 25 : female",
                        "V2 Person details - Email: jane@example.com, Phone: +1-555-0123",
                        true),
                Arguments.of(
                        "Minimal fields",
                        "john",
                        "30",
                        null,
                        null,
                        null,
                        "Person received : john : 30 : ",
                        null,
                        false),
                Arguments.of(
                        "Email only",
                        "alice",
                        "28",
                        null,
                        "alice@test.com",
                        null,
                        "Person received : alice : 28 : ",
                        "V2 Person details - Email: alice@test.com, Phone: null",
                        true),
                Arguments.of(
                        "Phone only",
                        "bob",
                        "35",
                        null,
                        null,
                        "+1-555-9876",
                        "Person received : bob : 35 : ",
                        "V2 Person details - Email: null, Phone: +1-555-9876",
                        true));
    }

    @Test
    void publishV2PersonWithoutName() {
        this.mockMvcTester
                .post()
                .uri("/person/v2/publish")
                .param("age", "33")
                .param("email", "test@example.com")
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
    void publishV2PersonWithoutAge() {
        this.mockMvcTester
                .post()
                .uri("/person/v2/publish")
                .param("name", "test")
                .param("email", "test@example.com")
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
    void publishV2PersonWithEmptyName() {
        this.mockMvcTester
                .post()
                .uri("/person/v2/publish")
                .param("name", "")
                .param("age", "33")
                .param("email", "test@example.com")
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(this::assertBadRequestProblem);
    }

    @Test
    void publishV2PersonWithNegativeAge() {
        this.mockMvcTester
                .post()
                .uri("/person/v2/publish")
                .param("name", "test")
                .param("age", "-1")
                .param("email", "test@example.com")
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(this::assertBadRequestProblem);
    }

    private void assertBadRequestProblem(ProblemDetail problemDetail, String expectedDetail) {
        assertThat(problemDetail.getStatus()).isEqualTo(400);
        assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
        assertThat(problemDetail.getDetail()).contains(expectedDetail);
    }

    private void assertBadRequestProblem(ProblemDetail problemDetail) {
        assertThat(problemDetail.getStatus()).isEqualTo(400);
        assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
        assertThat(problemDetail.getDetail()).isEqualTo("Validation failure");
    }
}
