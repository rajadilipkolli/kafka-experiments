package com.sivalabs.springbootkafkaavro;

import com.sivalabs.springbootkafkaavro.model.Person;
import com.sivalabs.springbootkafkaavro.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
@Slf4j
public class SpringBootKafkaAvroApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(SpringBootKafkaAvroApplication.class, args);
  }

  @Autowired KafkaTemplate<String, Person> kafkaTemplate;

  @Autowired PersonRepository personRepository;

  @Override
  public void run(String... args) {
    Person person = Person.newBuilder().setId(1).setName("Siva").setAge(33).build();
    kafkaTemplate.send("persons", person);
  }

  @KafkaListener(topics = "persons")
  public void handler(ConsumerRecord<String, Person> personConsumerRecord) {
    Person person = personConsumerRecord.value();
    log.info(" {} : {} ", person.getName(), person.getAge());
    this.personRepository.save(person);
  }
}
