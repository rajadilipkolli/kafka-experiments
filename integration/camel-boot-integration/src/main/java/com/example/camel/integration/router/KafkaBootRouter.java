package com.example.camel.integration.router;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaBootRouter extends RouteBuilder {

    @Override
    public void configure() {
        // Kafka Producer - create a Map and marshal to JSON before sending
        from("timer://foo?period=1000")
            .routeId("kafka-producer-route")
            .process(exchange -> {
                Object fired = exchange.getIn().getHeader("firedTime");
                String time;
                if (fired == null) {
                    time = Instant.now().toString();
                } else if (fired instanceof Date) {
                    time = fired.toString();
                } else {
                    time = fired.toString();
                }

                Map<String, Object> payload = new HashMap<>();
                payload.put("message", "Hello Kafka at " + time);
                exchange.getIn().setBody(payload);
            })
            .marshal().json()
            .setHeader(KafkaConstants.KEY, constant("Camel")) // Key of the message
            .to("kafka:myTopic");

        // Kafka Consumer - unmarshal JSON to Map and process
        from("kafka:myTopic")
            .routeId("kafka-consumer-route")
            .unmarshal().json(Map.class)
                .process(exchange -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> body = exchange.getIn().getBody(Map.class);
                    System.out.println("Message received from Kafka (as Map): " + body);
                })
            .log("    on the topic ${headers[kafka.TOPIC]}")
            .log("    on the partition ${headers[kafka.PARTITION]}")
            .log("    with the offset ${headers[kafka.OFFSET]}")
            .log("    with the key ${headers[kafka.KEY]}");
    }
}
