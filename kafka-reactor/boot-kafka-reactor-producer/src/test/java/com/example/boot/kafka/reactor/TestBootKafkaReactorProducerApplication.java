package com.example.boot.kafka.reactor;

import com.example.boot.kafka.reactor.common.ContainerConfiguration;
import org.springframework.boot.SpringApplication;

class TestBootKafkaReactorProducerApplication {

    public static void main(String[] args) {
        SpringApplication.from(BootKafkaReactorProducerApplication::main)
                .with(ContainerConfiguration.class)
                .run(args);
    }
}
