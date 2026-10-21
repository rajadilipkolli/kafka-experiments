package com.example.outboxpattern.config;

import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Recovers stranded publications for Modulith's bounded FAILED-publication replay. */
@Repository
class OutboxEventPublicationRecovery {

    private final JdbcClient jdbcClient;

    OutboxEventPublicationRecovery(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    int recoverStaleResubmissions(Instant cutoff, int limit) {
        return jdbcClient
                .sql(
                        """
                        UPDATE event_publication
                        SET status = 'FAILED'
                        WHERE id IN (
                            SELECT id FROM event_publication
                            WHERE status = 'RESUBMITTED'
                              AND completion_date IS NULL
                              AND COALESCE(last_resubmission_date, publication_date) < :cutoff
                            ORDER BY COALESCE(last_resubmission_date, publication_date), id
                            LIMIT :limit
                            FOR UPDATE SKIP LOCKED
                        )
                        """)
                .param("cutoff", Timestamp.from(cutoff))
                .param("limit", limit)
                .update();
    }

    int recoverStalePublications(Instant cutoff, int limit) {
        return jdbcClient
                .sql(
                        """
                        UPDATE event_publication
                        SET status = 'FAILED'
                        WHERE id IN (
                            SELECT id FROM event_publication
                            WHERE status IN ('PUBLISHED', 'PROCESSING')
                              AND completion_date IS NULL
                              AND COALESCE(last_resubmission_date, publication_date) < :cutoff
                            ORDER BY COALESCE(last_resubmission_date, publication_date), id
                            LIMIT :limit
                            FOR UPDATE SKIP LOCKED
                        )
                        """)
                .param("cutoff", Timestamp.from(cutoff))
                .param("limit", limit)
                .update();
    }
}
