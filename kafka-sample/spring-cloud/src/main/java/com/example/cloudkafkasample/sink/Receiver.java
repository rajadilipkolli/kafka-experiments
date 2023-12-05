/* (C)2023 */
package com.example.cloudkafkasample.sink;

import com.example.cloudkafkasample.domain.MessageDTO;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.BatchListenerFailedException;

@Configuration(proxyBeanMethods = false)
public class Receiver {

    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    private final CountDownLatch latch = new CountDownLatch(1);

    public CountDownLatch getLatch() {
        return latch;
    }

    @Bean
    public Consumer<List<MessageDTO>> receive() {
        return batchMessages -> {
            AtomicInteger i = new AtomicInteger();
            logger.info("Number of messages in batch : {}", batchMessages.size());
            batchMessages.forEach(messageDTO -> {
                logger.info("Data received from input-topic... {}", messageDTO);
                if (!messageDTO.msg().isBlank()) {
                    latch.countDown();
                } else {
                    logger.error("Unable to process hence throwing exception");
                    throw new BatchListenerFailedException("Unable to process", i.get());
                }
                i.incrementAndGet();
            });
        };
    }
}
