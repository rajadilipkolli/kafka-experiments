package com.example.quarkuskafkademo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

@QuarkusTest
@QuarkusTestResource(KafkaTestResource.class)
public class ProducerConsumerIT {

    @TestHTTPResource("/produce")
    URI produceUri;

    @Test
    public void testProduceAndConsumePojo() {
        String json = "{\"message\":\"hello-test-containers\",\"id\":123}";

        RestAssured.given()
                .baseUri(produceUri.toString() + "/pojo")
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post()
                .then()
                .statusCode(202);

        // wait up to 10 seconds for the consumer to receive the message
        String received = KafkaConsumerBean.pollMessage(10, TimeUnit.SECONDS);
        assertThat("Expected a message to be consumed", received, notNullValue());

        // Compare JSON semantically
        JsonMapper mapper = new JsonMapper();
        assertThat(mapper.readTree(received), equalTo(mapper.readTree(json)));
    }
}
