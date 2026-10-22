package com.example.outboxpattern.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.outboxpattern.common.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JdbcOutboxStatsServiceIT extends AbstractIntegrationTest {

    @BeforeEach
    void setUp() {
        jdbcClient.sql("DELETE FROM outbox_record").update();
    }

    @Test
    void agedFailuresAffectHealthUntilResolved() {
        jdbcClient
                .sql(
                        """
                        INSERT INTO outbox_record (
                            id, status, record_key, record_type, payload, created_at, failure_count, next_retry_at, partition_no, handler_id
                        )
                        SELECT
                            'failed-' || i,
                            'FAILED',
                            'key-' || i,
                            'order',
                            '{}',
                            NOW() - INTERVAL '2 days',
                            1,
                            NOW(),
                            0,
                            'handler'
                        FROM generate_series(1, 51) AS i
                        """)
                .update();
        jdbcClient
                .sql(
                        """
                        INSERT INTO outbox_record (
                            id, status, record_key, record_type, payload, created_at, failure_count, next_retry_at, partition_no, handler_id
                        )
                        VALUES
                            ('new-1', 'NEW', 'key-new', 'order', '{}', NOW(), 0, NOW(), 0, 'handler'),
                            ('completed-1', 'COMPLETED', 'key-completed', 'order', '{}', NOW(), 0, NOW(), 0, 'handler')
                        """)
                .update();

        JdbcOutboxStatsService service = new JdbcOutboxStatsService(jdbcClient);
        OutboxHealthIndicator indicator = new OutboxHealthIndicator(service);

        assertThat(service.getStats()).isEqualTo(new OutboxStats(1L, 51L));
        Health health = indicator.health();
        assertThat(health).isNotNull();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("failedPublications", 51L);

        jdbcClient
                .sql("UPDATE outbox_record SET status = 'COMPLETED' WHERE status = 'FAILED'")
                .update();

        assertThat(service.getStats()).isEqualTo(new OutboxStats(1L, 0L));
        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
    }
}
