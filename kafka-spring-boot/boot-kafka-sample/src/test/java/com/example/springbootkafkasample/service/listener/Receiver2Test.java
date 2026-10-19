package com.example.springbootkafkasample.service.listener;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.springbootkafkasample.dto.MessageDTO;
import org.junit.jupiter.api.Test;

class Receiver2Test {

    @Test
    void shouldTrackSeenMessagesAndEvictOldestWhenFull() {
        Receiver2 receiver = new Receiver2(3);

        receiver.listenTopic2(new MessageDTO("test", "msg1"), "test");
        receiver.listenTopic2(new MessageDTO("test", "msg2"), "test");
        receiver.listenTopic2(new MessageDTO("test", "msg3"), "test");

        assertThat(receiver.getSeenMessagesCount()).isEqualTo(3);
        assertThat(receiver.hasSeenMessage("msg1")).isTrue();
        assertThat(receiver.hasSeenMessage("msg2")).isTrue();
        assertThat(receiver.hasSeenMessage("msg3")).isTrue();

        // Adding 4th message should evict msg1 (the eldest)
        receiver.listenTopic2(new MessageDTO("test", "msg4"), "test");
        assertThat(receiver.getSeenMessagesCount()).isEqualTo(3);
        assertThat(receiver.hasSeenMessage("msg1")).isFalse();
        assertThat(receiver.hasSeenMessage("msg2")).isTrue();
        assertThat(receiver.hasSeenMessage("msg3")).isTrue();
        assertThat(receiver.hasSeenMessage("msg4")).isTrue();
    }

    @Test
    void shouldIncrementCountForExistingMessageWithoutEviction() {
        Receiver2 receiver = new Receiver2(2);

        receiver.listenTopic2(new MessageDTO("test", "msg1"), "test");
        receiver.listenTopic2(new MessageDTO("test", "msg1"), "test");

        assertThat(receiver.getSeenMessagesCount()).isEqualTo(1);
        assertThat(receiver.hasSeenMessage("msg1")).isTrue();
    }
}
