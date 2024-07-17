package com.example.outboxpattern;

import com.example.outboxpattern.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
