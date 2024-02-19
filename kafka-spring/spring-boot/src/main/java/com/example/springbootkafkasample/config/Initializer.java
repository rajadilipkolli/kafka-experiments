package com.example.springbootkafkasample.config;

import com.example.springbootkafkasample.dto.MessageDTO;
import com.example.springbootkafkasample.service.sender.Sender;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Initializer implements CommandLineRunner {
    public static final String TOPIC_TEST_1 = "test_1";

    public static final String TOPIC_TEST_2 = "test_2";
    private static final String TOPIC_TEST_3 = "test_3";

    private final Sender sender;

    public Initializer(Sender sender) {
        this.sender = sender;
    }

    @Override
    public void run(String... args) {
        for (int i = 0; i < 10; i++) {
            if (i % 3 == 0) {
                this.sender.send(new MessageDTO(TOPIC_TEST_1, String.valueOf(i)));
            } else if (i % 3 == 1) {
                this.sender.send(new MessageDTO(TOPIC_TEST_2, String.valueOf(i)));
            } else {
                this.sender.send(new MessageDTO(TOPIC_TEST_3, String.valueOf(i)));
            }
        }
    }
}
