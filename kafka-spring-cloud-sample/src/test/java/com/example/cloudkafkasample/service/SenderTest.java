/* (C)2026 */
package com.example.cloudkafkasample.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.cloudkafkasample.domain.MessageDTO;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.server.ResponseStatusException;

class SenderTest {

    private final StreamBridge bridge = mock(StreamBridge.class);
    private final Sender sender = new Sender(bridge);
    private final MessageDTO message = new MessageDTO("test");

    @Test
    void successfulSendReturnsNormally() {
        when(bridge.send("receive-out-0", message, MediaType.APPLICATION_JSON)).thenReturn(true);
        assertThatCode(() -> sender.send(message)).doesNotThrowAnyException();
    }

    @Test
    void rejectedSendReturns503() {
        when(bridge.send("receive-out-0", message, MediaType.APPLICATION_JSON)).thenReturn(false);
        assertThatThrownBy(() -> sender.send(message))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> assertThat(ex.getStatusCode())
                        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @ParameterizedTest
    @MethodSource("sendFailures")
    void binderFailureReturns503(MessagingException failure) {
        when(bridge.send("receive-out-0", message, MediaType.APPLICATION_JSON)).thenThrow(failure);
        assertThatThrownBy(() -> sender.send(message)).isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            assertThat(ex.getCause()).isSameAs(failure);
        });
    }

    static Stream<MessagingException> sendFailures() {
        return Stream.of(
                new MessageDeliveryException(new GenericMessage<>("test"), "unavailable"),
                new MessageHandlingException(new GenericMessage<>("test"), new IllegalStateException("send failed")));
    }

    @Test
    void unrelatedApplicationFailurePropagates() {
        IllegalArgumentException failure = new IllegalArgumentException("invalid message");
        when(bridge.send("receive-out-0", message, MediaType.APPLICATION_JSON)).thenThrow(failure);
        assertThatThrownBy(() -> sender.send(message)).isSameAs(failure);
    }
}
