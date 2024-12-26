package com.example.kafka.processor;

import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public ToUpperStringProcessor(String id) {
        this.id = id;
    }

    /**
     * Initializes the ToUpperStringProcessor instance.
     * This method logs the creation of a new ToUpperStringProcessor instance with its unique ID.
     */
    @PostConstruct
    public void init() {
        log.info("Initializing new To Upper String Processor with id: {}", this.id);
    }

    /**
     * Processes and stores a string value in a queue.
     * Logs the processing action and handles any potential interruption during the process.
     *
     * @param value The string value to be processed.
     */
    public void processString(String value) {
        log.info("Instance {} processing new string {}.", this.id, value);
        try {
            this.queue.put(value.toUpperCase());
        } catch (InterruptedException exception) {
            log.error("Thread has been interrupted.", exception);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Retrieves the current size of the queue.
     *
     * @return The number of elements in the queue.
     */
    public int queueSize() {
        return this.queue.size();
    }

    /**
     * Returns a set of distinct strings from the queue.
     * This method is useful for retrieving all unique values that have been processed.
     *
     * @return A Set of distinct strings.
     */
    public Set<String> distinctQueuedData() {
        return new HashSet<>(this.queue);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ToUpperStringProcessor.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .toString();
    }
}
