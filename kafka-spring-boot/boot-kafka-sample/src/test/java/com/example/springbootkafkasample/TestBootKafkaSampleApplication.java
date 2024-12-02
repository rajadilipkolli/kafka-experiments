package com.example.springbootkafkasample;

import com.example.springbootkafkasample.common.ContainerConfig;
import org.springframework.boot.SpringApplication;

public class TestBootKafkaSampleApplication {

    public static void main(String[] args) {
        SpringApplication.from(BootKafkaSampleApplication::main)
                .with(ContainerConfig.class)
                .run(args);
    }
}
