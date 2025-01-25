package com.example.springbootkafkaavro;

import com.example.springbootkafkaavro.model.Person;
import com.example.springbootkafkaavro.util.ApplicationConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.kafka.annotation.KafkaListener;

@TestComponent
public class AvroKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(AvroKafkaListener.class);

    @KafkaListener(topics = ApplicationConstants.PERSONS_TOPIC, groupId = "group_id")
    public void handler(ConsumerRecord<String, Person> personConsumerRecord) {
        Person person = personConsumerRecord.value();
        log.info("Person received : {} : {} ", person.getName(), person.getAge());
    }
}
