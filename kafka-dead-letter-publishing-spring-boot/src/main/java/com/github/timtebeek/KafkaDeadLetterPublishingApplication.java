package com.github.timtebeek;

import com.github.timtebeek.config.AppKafkaProperties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppKafkaProperties.class)
public class KafkaDeadLetterPublishingApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaDeadLetterPublishingApplication.class, args);
    }
}
