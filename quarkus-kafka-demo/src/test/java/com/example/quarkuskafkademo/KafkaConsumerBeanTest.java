package com.example.quarkuskafkademo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KafkaConsumerBeanTest {

    private KafkaConsumerBean consumerBean;

    @BeforeEach
    void setUp() {
        consumerBean = new KafkaConsumerBean();
        KafkaConsumerBean.setCapacityForTesting(3);
    }

    @AfterEach
    void tearDown() {
        KafkaConsumerBean.setCapacityForTesting(KafkaConsumerBean.DEFAULT_CAPACITY);
    }

    @Test
    void shouldDropOldestWhenQueueIsFull() {
        consumerBean.receive("msg1");
        consumerBean.receive("msg2");
        consumerBean.receive("msg3");
        assertThat(KafkaConsumerBean.getReceivedCount(), equalTo(3));

        // When 4th message arrives at capacity 3, msg1 should be dropped
        consumerBean.receive("msg4");
        assertThat(KafkaConsumerBean.getReceivedCount(), equalTo(3));

        assertThat(KafkaConsumerBean.pollMessage(1, TimeUnit.SECONDS), equalTo("msg2"));
        assertThat(KafkaConsumerBean.pollMessage(1, TimeUnit.SECONDS), equalTo("msg3"));
        assertThat(KafkaConsumerBean.pollMessage(1, TimeUnit.SECONDS), equalTo("msg4"));
    }
}
