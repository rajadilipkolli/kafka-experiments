package com.example.cloudkafkasample.sink;

import com.example.cloudkafkasample.domain.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

@Configuration(proxyBeanMethods = false)
public class Receiver {

    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    private final CountDownLatch latch = new CountDownLatch(1);

    public CountDownLatch getLatch() {
        return latch;
    }

    @Bean
    public Consumer<MessageDTO> receive() {
        return data -> {
            logger.info("Data received from topic-1... {}", data);
            latch.countDown();
        };

    }
}
