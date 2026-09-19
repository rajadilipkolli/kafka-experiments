/* (C)2023 */
package com.example.cloudkafkasample;

import com.example.cloudkafkasample.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

class TestCloudKafkaSampleApplication {

    public static void main(String[] args) {
        SpringApplication.from(CloudKafkaSampleApplication::main)
                .with(ContainersConfig.class)
                .run(args);
    }
}
