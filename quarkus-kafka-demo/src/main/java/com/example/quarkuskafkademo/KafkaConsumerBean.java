package com.example.quarkuskafkademo;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class KafkaConsumerBean {

    private static final Logger LOG = Logger.getLogger(KafkaConsumerBean.class);
    public static final int DEFAULT_CAPACITY = 1000;
    // Queue used by tests to assert received messages. Production can ignore it.
    private static BlockingQueue<String> RECEIVED = new LinkedBlockingQueue<>(DEFAULT_CAPACITY);

    @Incoming("demo-in")
    public void receive(String message) {
        LOG.infof("Received: %s", message);
        synchronized (RECEIVED) {
            if (!RECEIVED.offer(message)) {
                RECEIVED.poll();
                RECEIVED.offer(message);
            }
        }
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

    public static int getReceivedCount() {
        return RECEIVED.size();
    }

    public static void setCapacityForTesting(int capacity) {
        RECEIVED = new LinkedBlockingQueue<>(capacity);
    }
}
