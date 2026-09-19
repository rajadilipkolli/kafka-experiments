package com.example.springbootkafka.multi;

import com.example.springbootkafka.multi.common.ContainerConfiguration;
import org.springframework.boot.SpringApplication;

class TestSpringBootKafkaMultiApplication {

    public static void main(String[] args) {
        SpringApplication.from(SpringBootKafkaMultiApplication::main)
                .with(ContainerConfiguration.class)
                .run(args);
    }
}
