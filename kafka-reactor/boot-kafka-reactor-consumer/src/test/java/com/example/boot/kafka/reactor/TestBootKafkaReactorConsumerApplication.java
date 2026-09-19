package com.example.boot.kafka.reactor;

import com.example.boot.kafka.reactor.common.ContainerConfiguration;
import com.example.boot.kafka.reactor.common.TestKafkaProducer;
import org.springframework.boot.SpringApplication;

class TestBootKafkaReactorConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.from(BootKafkaReactorConsumerApplication::main)
                .with(ContainerConfiguration.class, TestKafkaProducer.class)
                .run(args);
    }
}
