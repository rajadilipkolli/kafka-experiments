package com.example.outboxpattern;

import com.example.outboxpattern.common.ContainersConfig;
import com.example.outboxpattern.common.SQLContainerConfig;
import org.springframework.boot.SpringApplication;

public class TestModulithApplication {

    public static void main(String[] args) {
        SpringApplication.from(ModulithApplication::main)
                .with(ContainersConfig.class, SQLContainerConfig.class)
                .withAdditionalProfiles("local")
                .run(args);
    }
}
