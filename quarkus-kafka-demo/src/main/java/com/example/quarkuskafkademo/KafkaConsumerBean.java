package com.example.quarkuskafkademo;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class KafkaConsumerBean {

    // Queue used by tests to assert received messages. Production can ignore it.
    private static final BlockingQueue<String> RECEIVED = new LinkedBlockingQueue<>();

    @Incoming("words-in")
    public void receive(String message) {
        System.out.println("[KafkaConsumer] Received: " + message);
        RECEIVED.offer(message);
    }

    /**
     * Poll a received message for tests.
     *
     * @param timeout timeout amount
     * @param unit    time unit
     * @return message or null if none received in time
     */
    public static String pollMessage(long timeout, TimeUnit unit) {
        try {
            return RECEIVED.poll(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
