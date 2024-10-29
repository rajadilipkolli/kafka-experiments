/* Licensed under Apache-2.0 2023-2024 */
package com.example.analytics;

import com.example.analytics.common.ContainersConfiguration;
import org.springframework.boot.SpringApplication;

class TestAnalyticsConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.from(AnalyticsConsumerApplication::main)
                .with(ContainersConfiguration.class)
                .run(args);
    }
}
