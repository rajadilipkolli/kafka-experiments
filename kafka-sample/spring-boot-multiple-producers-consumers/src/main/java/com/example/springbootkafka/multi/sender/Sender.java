package com.example.springbootkafka.multi.sender;

import static com.example.springbootkafka.multi.util.AppConstants.TOPIC_TEST_1;
import static com.example.springbootkafka.multi.util.AppConstants.TOPIC_TEST_2;

import com.example.springbootkafka.multi.domain.SimpleMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.RoutingKafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class Sender {

    private final RoutingKafkaTemplate routingKafkaTemplate;

    public void send(Integer key, String msg) {
        this.routingKafkaTemplate.send(TOPIC_TEST_1, key, msg);
        log.info("Sent key= {}, msg= {} to topic: {}", key, msg, TOPIC_TEST_1);
    }

    public void send(SimpleMessage msg) {
        String key = String.valueOf(msg.key());
        this.routingKafkaTemplate.send(TOPIC_TEST_2, key, msg);
        log.info("Sent key= {}, msg= {} to topic: {}", key, msg, TOPIC_TEST_2);
    }
}
