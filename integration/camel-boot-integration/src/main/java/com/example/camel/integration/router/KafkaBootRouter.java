package com.example.camel.integration.router;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;

@Component
public class KafkaBootRouter extends RouteBuilder {

    @Override
    public void configure() {
        // Kafka Producer
        from("timer://foo?period=1000")
                .routeId("kafka-producer-route")
                .setBody(simple("Hello Kafka at ${header.firedTime}"))
                .setHeader(KafkaConstants.KEY, constant("Camel")) // Key of the message
                .to("kafka:myTopic");

        // Kafka Consumer
        from("kafka:myTopic")
                .log("Message received from Kafka : ${body}")
                .log("    on the topic ${headers[kafka.TOPIC]}")
                .log("    on the partition ${headers[kafka.PARTITION]}")
                .log("    with the offset ${headers[kafka.OFFSET]}")
                .log("    with the key ${headers[kafka.KEY]}");
    }
}
