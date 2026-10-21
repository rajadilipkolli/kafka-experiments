package com.example.outboxpattern.config;

/** Contract for retrieving outbox operational statistics. */
public interface OutboxStatsService {

    /** Returns current outbox statistics for pending and all unresolved failed records. */
    OutboxStats getStats();
}
