package com.example.outboxpattern.config;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
class JdbcOutboxStatsService implements OutboxStatsService {

    static final String PENDING_QUERY = "SELECT count(*) FROM outbox_record WHERE status = 'NEW'";
    static final String FAILED_QUERY = "SELECT count(*) FROM outbox_record WHERE status = 'FAILED'";

    private final JdbcClient jdbcClient;

    /** Creates a service that reads outbox counts using the supplied database client. */
    JdbcOutboxStatsService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * Returns counts of {@code NEW} records as pending and all {@code FAILED} records as failed,
     * regardless of age. The counts are queried separately.
     *
     * @throws org.springframework.dao.DataAccessException if either count query fails
     */
    @Override
    public OutboxStats getStats() {
        long pending = jdbcClient.sql(PENDING_QUERY).query(Long.class).single();
        long failed = jdbcClient.sql(FAILED_QUERY).query(Long.class).single();

        return new OutboxStats(pending, failed);
    }
}
