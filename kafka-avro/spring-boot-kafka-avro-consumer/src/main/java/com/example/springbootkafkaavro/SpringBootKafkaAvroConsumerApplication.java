package com.example.springbootkafkaavro;

import com.example.springbootkafkaavro.model.Person;
import org.apache.avro.util.ClassSecurityValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootKafkaAvroConsumerApplication {

    static {
        ClassSecurityValidator.setGlobal(
                ClassSecurityValidator.composite(
                        ClassSecurityValidator.getGlobal(),
                        ClassSecurityValidator.builder().add(Person.class).build()));
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootKafkaAvroConsumerApplication.class, args);
    }
}
