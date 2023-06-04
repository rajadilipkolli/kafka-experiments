package com.example.springbootkafkasample;

import com.example.springbootkafkasample.dto.MessageDTO;
import com.example.springbootkafkasample.sender.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootKafkaSampleApplication implements CommandLineRunner {

    public static final String TOPIC_TEST_1 = "test_1";
    public static final String TOPIC_TEST_2 = "test_2";

    @Autowired
    Sender sender;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootKafkaSampleApplication.class, args);
    }

    @Override
    public void run(String... args) {
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                this.sender.send(new MessageDTO(TOPIC_TEST_1, String.valueOf(i)));
            } else {
                this.sender.send(new MessageDTO(TOPIC_TEST_2, String.valueOf(i)));
            }
        }
    }
}