package com.sivalabs.springbootkafka.multi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootKafkaMultiApplication {

    static final String TOPIC_TEST_1 = "test_1";
    static final String TOPIC_TEST_2 = "test_2";

    public static void main(String[] args) {
        SpringApplication.run(SpringBootKafkaMultiApplication.class, args).close();
    }
}