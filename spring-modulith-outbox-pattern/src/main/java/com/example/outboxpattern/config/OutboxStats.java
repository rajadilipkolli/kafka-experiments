package com.example.outboxpattern.config;

/**
 * Metric summary of pending and recent failed outbox records.
 *
 * @param pendingCount count of records awaiting processing
 * @param failedCount count of failed records within the configured time window
 */
public record OutboxStats(long pendingCount, long failedCount) {}
