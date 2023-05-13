/* Licensed under Apache-2.0 2023 */
package com.example.analytics;

import com.example.analytics.config.KafkaTestContainersConfiguration;

import org.springframework.boot.SpringApplication;

public class TestAnalyticsConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.from(AnalyticsConsumerApplication::main)
                .with(KafkaTestContainersConfiguration.class)
                .run(args);
    }
}
