package com.example.quarkuskafkademo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(KafkaTestResource.class)
public class ProducerConsumerIT {

    @TestHTTPResource("/produce")
    URI produceUri;

    @Test
    public void testProduceAndConsumePojo()
            throws JsonMappingException, JsonProcessingException, IllegalArgumentException {
        MessageDto dto = new MessageDto("hello-test-containers", 123);

        RestAssured.given()
                .baseUri(produceUri.toString() + "/pojo")
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post()
                .then()
                .statusCode(202);

        // wait up to 10 seconds for the consumer to receive the message
        String received = KafkaConsumerBean.pollMessage(10, TimeUnit.SECONDS);
        assertThat("Expected a message to be consumed", received, notNullValue());

        // Compare JSON semantically
        ObjectMapper om = new ObjectMapper();
        assertThat(om.readTree(received), equalTo(om.valueToTree(dto)));
    }
}
