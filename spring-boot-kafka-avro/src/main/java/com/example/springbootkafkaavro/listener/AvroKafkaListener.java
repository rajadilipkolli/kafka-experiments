package com.example.springbootkafkaavro.listener;

import com.example.springbootkafkaavro.entity.PersonEntity;
import com.example.springbootkafkaavro.model.Person;
import com.example.springbootkafkaavro.repository.PersonRepository;
import com.example.springbootkafkaavro.util.ApplicationConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AvroKafkaListener {

    private final PersonRepository personRepository;

    @KafkaListener(topics = ApplicationConstants.PERSONS_TOPIC, groupId = "group_id")
    public void handler(ConsumerRecord<String, Person> personConsumerRecord) {
        Person person = personConsumerRecord.value();
        log.info("Person received : {} : {} ", person.getName(), person.getAge());
        PersonEntity personEntity =
                new PersonEntity(null, person.getName().toString(), person.getAge());
        this.personRepository.save(personEntity);
    }
}
