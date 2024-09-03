package com.example.outboxpattern;

import com.example.outboxpattern.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestModulithApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "local");
        SpringApplication.from(ModulithApplication::main)
                .with(ContainersConfig.class)
                .run(args);
    }
}
