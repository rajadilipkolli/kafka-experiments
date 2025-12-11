package com.example.camel.integration.router;

import java.time.Instant;
import java.util.Date;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;

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
                    exchange.getIn().setBody(new KafkaMessage("Hello Kafka at " + time));
                })
                .marshal()
                .json()
                .setHeader(KafkaConstants.KEY, constant("Camel")) // Key of the message
                .to("kafka:myTopic");

        // Kafka Consumer - unmarshal JSON to Map and process
        from("kafka:myTopic")
                .routeId("kafka-consumer-route")
                .unmarshal()
                .json(KafkaMessage.class)
                .log(" Received message from Kafka:")
                .log("    message: ${body.message}")
                .log("    on the topic ${headers[kafka.TOPIC]}")
                .log("    on the partition ${headers[kafka.PARTITION]}")
                .log("    with the offset ${headers[kafka.OFFSET]}")
                .log("    with the key ${headers[kafka.KEY]}");
    }

    private record KafkaMessage(String message) {}
}
