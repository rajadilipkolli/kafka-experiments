package com.example.springbootkafkaavro;

import com.example.springbootkafkaavro.model.Person;
import com.example.springbootkafkaavro.util.ApplicationConstants;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.kafka.core.KafkaTemplate;

@TestComponent
public class KafkaProducer {

    private final KafkaTemplate<String, Person> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, Person> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(Person person) {
        this.kafkaTemplate.send(
                ApplicationConstants.PERSONS_TOPIC, person.getName().toString(), person);
    }
}
