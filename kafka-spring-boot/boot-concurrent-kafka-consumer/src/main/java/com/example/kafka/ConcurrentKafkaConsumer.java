package com.example.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConcurrentKafkaConsumer {

    public static void main(String[] args) {
        SpringApplication.run(ConcurrentKafkaConsumer.class, args);
    }
}
