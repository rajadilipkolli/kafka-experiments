package com.example.springbootkafkasample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootKafkaSampleApplication implements CommandLineRunner {

    static final String TOPIC_TEST_1 = "test_1";
    static final String TOPIC_TEST_2 = "test_2";

    @Autowired
    Sender sender;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootKafkaSampleApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                this.sender.send(TOPIC_TEST_1, String.valueOf(i));
            } else {
                this.sender.send(TOPIC_TEST_2, String.valueOf(i));
            }
        }
    }
}