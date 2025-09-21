package com.example.springbootkafkasample.service.listener;

import static com.example.springbootkafkasample.config.Initializer.TOPIC_TEST_2;

import com.example.springbootkafkasample.dto.MessageDTO;
import jakarta.validation.Valid;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.stereotype.Component;

@Component
public class Receiver2 {

    private static final Logger logger = LoggerFactory.getLogger(Receiver2.class);

    private final CountDownLatch latch = new CountDownLatch(10);

    private final CountDownLatch deadLetterLatch = new CountDownLatch(1);

    public CountDownLatch getLatch() {
        return latch;
    }

    public CountDownLatch getDeadLetterLatch() {
        return deadLetterLatch;
    }

    @RetryableTopic(
            attempts = "2",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            exclude = {MethodArgumentNotValidException.class},
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(id = "topic_2_Listener", topics = TOPIC_TEST_2, groupId = "foo")
    public void listenTopic2(@Payload @Valid MessageDTO messageDTO, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        logger.info("Received message : {} in topic :{}", messageDTO.toString(), topic);
        latch.countDown();
    }

    @DltHandler
    public void dlt(MessageDTO messageDTO, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        logger.error("{} from {}", messageDTO, topic);
        deadLetterLatch.countDown();
    }
}
