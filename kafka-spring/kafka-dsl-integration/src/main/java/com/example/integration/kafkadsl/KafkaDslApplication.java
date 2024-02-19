package com.example.integration.kafkadsl;

import com.example.integration.kafkadsl.config.KafkaAppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(KafkaAppProperties.class)
public class KafkaDslApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaDslApplication.class, args);
    }
}
