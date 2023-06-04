package com.example.springbootkafkasample;

import com.example.springbootkafkasample.config.MyTestContainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestSpringBootKafkaSampleApplication {

    public static void main(String[] args) {
        SpringApplication.from(SpringBootKafkaSampleApplication::main).with(MyTestContainersConfiguration.class).run(args);
    }
}
