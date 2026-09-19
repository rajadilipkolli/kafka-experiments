package com.example.integration.kafkadsl;

import org.springframework.boot.SpringApplication;

public class TestKafkaDslApplication {

    public static void main(String[] args) {
        SpringApplication.from(KafkaDslApplication::main)
                .with(ContainerConfiguration.class)
                .run(args);
    }
}
