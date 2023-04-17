package com.sivalabs.springbootkafkaavro;

import com.sivalabs.springbootkafkaavro.model.Person;
import com.sivalabs.springbootkafkaavro.util.ApplicationConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
public class SpringBootKafkaAvroApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootKafkaAvroApplication.class, args);
    }

    @Autowired KafkaTemplate<String, Person> kafkaTemplate;

    @Override
    public void run(String... args) {
        Person person = Person.newBuilder().setId(1L).setName("Siva").setAge(33).build();
        kafkaTemplate.send(ApplicationConstants.PERSONS_TOPIC, person);
    }
}
