package com.example.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.example.analytics")
public class AnalyticsConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsConsumerApplication.class, args);
    }

}
