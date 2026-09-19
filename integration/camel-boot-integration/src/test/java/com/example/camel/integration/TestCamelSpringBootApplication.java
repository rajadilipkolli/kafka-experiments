package com.example.camel.integration;

import com.example.camel.integration.common.ContainerConfiguration;
import org.springframework.boot.SpringApplication;

class TestCamelSpringBootApplication {

    static void main(String[] args) {
        SpringApplication.from(CamelSpringBootApplication::main)
                .with(ContainerConfiguration.class)
                .run(args);
    }
}
