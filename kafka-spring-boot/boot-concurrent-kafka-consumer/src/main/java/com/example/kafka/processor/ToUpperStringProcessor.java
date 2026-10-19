package com.example.kafka.processor;

import jakarta.annotation.PostConstruct;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Processor class for handling string data.
 * This class is responsible for processing and storing string values.
 * It is scoped as a prototype bean in the Spring context.
 */
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class ToUpperStringProcessor {

    private static final Logger log = LoggerFactory.getLogger(ToUpperStringProcessor.class);

    private final String id;

    @Value("${app.processor.history-capacity:100}")
    private int historyCapacity = 100;

    private final Queue<String> queue = new ArrayDeque<>();
    private final AtomicLong processedCount = new AtomicLong();

    public ToUpperStringProcessor(String id) {
        this.id = id;
    }

    public ToUpperStringProcessor(String id, int historyCapacity) {
        this.id = id;
        this.historyCapacity = historyCapacity;
    }

    /**
     * Initializes the ToUpperStringProcessor instance.
     * This method logs the creation of a new ToUpperStringProcessor instance with its unique ID.
     */
    @PostConstruct
    public void init() {
        log.info("Initializing new To Upper String Processor with id: {}, capacity: {}", this.id, this.historyCapacity);
    }

    /**
     * Processes and stores a string value in a bounded history queue.
     * Evicts the oldest entry when full, then inserts the new value.
     *
     * @param value The string value to be processed.
     */
    public void processString(String value) {
        log.info("Instance {} processing new string {}.", this.id, value);
        this.processedCount.incrementAndGet();
        String upper = value.toUpperCase();
        synchronized (this.queue) {
            while (this.queue.size() >= this.historyCapacity) {
                this.queue.poll();
            }
            this.queue.offer(upper);
        }
    }

    /**
     * Retrieves the current size of the retained history.
     *
     * @return The number of elements in the queue.
     */
    public int queueSize() {
        synchronized (this.queue) {
            return this.queue.size();
        }
    }

    /**
     * Returns a set of distinct strings from the retained history queue.
     *
     * @return A Set of distinct strings.
     */
    public Set<String> distinctQueuedData() {
        synchronized (this.queue) {
            return new HashSet<>(this.queue);
        }
    }

    /**
     * Returns the total count of messages processed by this instance.
     *
     * @return Total count of processed messages.
     */
    public long processedCount() {
        return this.processedCount.get();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ToUpperStringProcessor.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("capacity=" + historyCapacity)
                .toString();
    }
}
