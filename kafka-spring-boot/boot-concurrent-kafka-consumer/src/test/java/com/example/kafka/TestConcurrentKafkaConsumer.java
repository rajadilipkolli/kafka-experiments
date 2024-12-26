package com.example.kafka;

import com.example.kafka.common.ContainerConfig;
import com.example.kafka.common.ProducerConfig;
import org.springframework.boot.SpringApplication;

public class TestConcurrentKafkaConsumer {

    public static void main(String[] args) {
        SpringApplication.from(ConcurrentKafkaConsumer::main)
                .with(ContainerConfig.class, ProducerConfig.class)
                .run(args);
    }
}
