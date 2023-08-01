/* (C)2023 */
package com.example.cloudkafkasample.sink;

import com.example.cloudkafkasample.domain.MessageDTO;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
            if (data.msg() != null) {
                logger.info("Data received from input-topic... {}", data);
                latch.countDown();
            } else {
                throw new RuntimeException();
            }
        };
    }
}
