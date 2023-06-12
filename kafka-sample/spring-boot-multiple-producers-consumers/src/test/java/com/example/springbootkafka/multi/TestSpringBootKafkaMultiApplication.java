package com.example.springbootkafka.multi;

import org.springframework.boot.SpringApplication;

import com.example.springbootkafka.multi.config.TestContainersConfiguration;

public class TestSpringBootKafkaMultiApplication {
    
    public static void main(String[] args) {
        SpringApplication.from(SpringBootKafkaMultiApplication::main).with(TestContainersConfiguration.class).run(args);
    }
}
