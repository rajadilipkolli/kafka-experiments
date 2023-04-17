package com.sivalabs.springbootkafkaavro.listener;

import com.sivalabs.springbootkafkaavro.entity.PersonEntity;
import com.sivalabs.springbootkafkaavro.model.Person;
import com.sivalabs.springbootkafkaavro.repository.PersonRepository;
import com.sivalabs.springbootkafkaavro.util.ApplicationConstants;

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

    @KafkaListener(topics = ApplicationConstants.PERSONS_TOPIC)
    public void handler(ConsumerRecord<String, Person> personConsumerRecord) {
        Person person = personConsumerRecord.value();
        log.info(" {} : {} ", person.getName(), person.getAge());
        PersonEntity personEntity =
                new PersonEntity(person.getId(), person.getName().toString(), person.getAge());
        this.personRepository.save(personEntity);
    }
}
