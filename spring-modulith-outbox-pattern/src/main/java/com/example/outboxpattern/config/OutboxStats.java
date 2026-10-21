package com.example.outboxpattern.config;

/**
 * Metric summary of pending and unresolved failed outbox records.
 *
 * @param pendingCount count of records awaiting processing
 * @param failedCount count of all unresolved failed records, regardless of age
 */
public record OutboxStats(long pendingCount, long failedCount) {}
