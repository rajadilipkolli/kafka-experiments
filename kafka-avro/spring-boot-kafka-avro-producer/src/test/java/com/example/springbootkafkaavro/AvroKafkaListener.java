package com.example.springbootkafkaavro;

import com.example.springbootkafkaavro.model.Person;
import com.example.springbootkafkaavro.util.ApplicationConstants;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.kafka.annotation.KafkaListener;

@TestComponent
@Slf4j
public class AvroKafkaListener {

    @KafkaListener(topics = ApplicationConstants.PERSONS_TOPIC, groupId = "group_id")
    public void handler(ConsumerRecord<String, Person> personConsumerRecord) {
        Person person = personConsumerRecord.value();
        log.info("Person received : {} : {} ", person.getName(), person.getAge());
    }
}
