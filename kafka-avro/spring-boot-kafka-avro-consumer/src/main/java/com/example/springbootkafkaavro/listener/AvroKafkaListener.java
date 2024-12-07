package com.example.springbootkafkaavro.listener;

import com.example.springbootkafkaavro.entity.PersonEntity;
import com.example.springbootkafkaavro.model.Person;
import com.example.springbootkafkaavro.repository.PersonRepository;
import com.example.springbootkafkaavro.util.ApplicationConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AvroKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(AvroKafkaListener.class);
    private final PersonRepository personRepository;

    public AvroKafkaListener(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @KafkaListener(topics = ApplicationConstants.PERSONS_TOPIC, groupId = "group_id")
    public void handler(ConsumerRecord<String, Person> personConsumerRecord) {
        Person person = personConsumerRecord.value();
        log.info("Person received : {} : {} ", person.getName(), person.getAge());
        PersonEntity personEntity =
                new PersonEntity().setName(person.getName().toString()).setAge(person.getAge());
        this.personRepository.save(personEntity);
    }
}
