package com.example.outboxpattern.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
class JdbcOutboxStatsServiceIT {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.6-alpine");

    @Test
    void agedFailuresAffectHealthUntilResolved() {
        JdbcClient jdbcClient = JdbcClient.create(
                new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()));
        jdbcClient
                .sql("CREATE TABLE outbox_record (status VARCHAR(20), created_at TIMESTAMPTZ)")
                .update();
        jdbcClient
                .sql(
                        """
                        INSERT INTO outbox_record (status, created_at)
                        SELECT 'FAILED', NOW() - INTERVAL '2 days' FROM generate_series(1, 51)
                        """)
                .update();
        jdbcClient
                .sql(
                        """
                        INSERT INTO outbox_record (status, created_at)
                        VALUES ('NEW', NOW()), ('COMPLETED', NOW())
                        """)
                .update();

        JdbcOutboxStatsService service = new JdbcOutboxStatsService(jdbcClient);
        OutboxHealthIndicator indicator = new OutboxHealthIndicator(service);

        assertThat(service.getStats()).isEqualTo(new OutboxStats(1L, 51L));
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("failedPublications", 51L);

        jdbcClient
                .sql("UPDATE outbox_record SET status = 'COMPLETED' WHERE status = 'FAILED'")
                .update();

        assertThat(service.getStats()).isEqualTo(new OutboxStats(1L, 0L));
        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
    }
}
