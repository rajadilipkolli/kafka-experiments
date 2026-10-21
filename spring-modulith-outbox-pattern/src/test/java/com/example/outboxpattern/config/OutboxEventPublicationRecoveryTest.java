package com.example.outboxpattern.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
class OutboxEventPublicationRecoveryTest {

    private static final Instant CUTOFF = Instant.parse("2026-10-19T12:00:00Z");
    private static final Instant OLD = CUTOFF.minusSeconds(3600);

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.6-alpine");

    private static DataSource dataSource;
    private static JdbcClient jdbc;
    private static OutboxEventPublicationRecovery recovery;

    @BeforeAll
    static void createSchema() {
        dataSource = new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        new ResourceDatabasePopulator(new ClassPathResource("db/changelog/migration/01-create_event_publication.sql"))
                .execute(dataSource);
        jdbc = JdbcClient.create(dataSource);
        recovery = new OutboxEventPublicationRecovery(jdbc);
    }

    @BeforeEach
    void clearPublications() {
        jdbc.sql("TRUNCATE event_publication").update();
    }

    @Test
    void recoversResubmissionsByLastAttemptAndFallsBackToPublicationDate() {
        UUID stale = publication("RESUBMITTED", OLD, OLD, null);
        UUID legacy = publication("RESUBMITTED", OLD, null, null);
        UUID recent = publication("RESUBMITTED", OLD, CUTOFF.plusSeconds(1), null);
        UUID boundary = publication("RESUBMITTED", OLD, CUTOFF, null);
        UUID fresh = publication("RESUBMITTED", CUTOFF, null, null);
        UUID completed = publication("RESUBMITTED", OLD, OLD, OLD);
        UUID published = publication("PUBLISHED", OLD, null, null);

        assertThat(recovery.recoverStaleResubmissions(CUTOFF, 2)).isEqualTo(2);

        assertThat(status(stale)).isEqualTo("FAILED");
        assertThat(status(legacy)).isEqualTo("FAILED");
        assertThat(status(recent)).isEqualTo("RESUBMITTED");
        assertThat(status(boundary)).isEqualTo("RESUBMITTED");
        assertThat(status(fresh)).isEqualTo("RESUBMITTED");
        assertThat(status(completed)).isEqualTo("RESUBMITTED");
        assertThat(status(published)).isEqualTo("PUBLISHED");
    }

    @Test
    void recoversResubmissionsInOldestFirstBatches() {
        UUID first = publication("RESUBMITTED", OLD, OLD.minusSeconds(1), null);
        UUID second = publication("RESUBMITTED", OLD, null, null);
        UUID third = publication("RESUBMITTED", OLD.minusSeconds(10), OLD.plusSeconds(1), null);

        assertThat(recovery.recoverStaleResubmissions(CUTOFF, 2)).isEqualTo(2);
        assertThat(status(first)).isEqualTo("FAILED");
        assertThat(status(second)).isEqualTo("FAILED");
        assertThat(status(third)).isEqualTo("RESUBMITTED");

        assertThat(recovery.recoverStaleResubmissions(CUTOFF, 2)).isEqualTo(1);
        assertThat(status(third)).isEqualTo("FAILED");
        assertThat(recovery.recoverStaleResubmissions(CUTOFF, 2)).isZero();
    }

    @Test
    void skipsResubmissionsLockedByAnotherWorker() throws Exception {
        UUID locked = publication("RESUBMITTED", OLD, OLD.minusSeconds(1), null);
        UUID available = publication("RESUBMITTED", OLD, OLD, null);

        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (var statement =
                    connection.prepareStatement("SELECT id FROM event_publication WHERE id = ? FOR UPDATE")) {
                statement.setObject(1, locked);
                statement.executeQuery().close();

                assertThat(recovery.recoverStaleResubmissions(CUTOFF, 1)).isEqualTo(1);
                assertThat(status(locked)).isEqualTo("RESUBMITTED");
                assertThat(status(available)).isEqualTo("FAILED");
            } finally {
                connection.rollback();
            }
        }
        assertThat(recovery.recoverStaleResubmissions(CUTOFF, 1)).isEqualTo(1);
        assertThat(status(locked)).isEqualTo("FAILED");
    }

    @Test
    void selectsOnlyEligiblePublicationsInOldestFirstBatches() {
        UUID first = publication("PROCESSING", OLD.minusSeconds(1), null, null);
        UUID second = publication("PUBLISHED", OLD, null, null);
        UUID third = publication("PROCESSING", OLD.plusSeconds(1), null, null);
        UUID boundary = publication("PUBLISHED", CUTOFF, null, null);
        UUID fresh = publication("PROCESSING", CUTOFF.plusSeconds(1), null, null);
        UUID resubmitted = publication("RESUBMITTED", OLD.minusSeconds(10), OLD, null);
        UUID failed = publication("FAILED", OLD.minusSeconds(10), null, null);
        UUID completed = publication("COMPLETED", OLD, null, OLD);
        UUID completedProcessing = publication("PROCESSING", OLD.minusSeconds(10), null, OLD);

        assertThat(recovery.recoverStalePublications(CUTOFF, 2)).isEqualTo(2);

        assertThat(status(first)).isEqualTo("FAILED");
        assertThat(status(second)).isEqualTo("FAILED");
        assertThat(status(third)).isEqualTo("PROCESSING");
        assertThat(status(boundary)).isEqualTo("PUBLISHED");
        assertThat(status(fresh)).isEqualTo("PROCESSING");
        assertThat(status(resubmitted)).isEqualTo("RESUBMITTED");
        assertThat(status(failed)).isEqualTo("FAILED");
        assertThat(status(completed)).isEqualTo("COMPLETED");
        assertThat(status(completedProcessing)).isEqualTo("PROCESSING");

        assertThat(recovery.recoverStalePublications(CUTOFF, 2)).isEqualTo(1);
        assertThat(status(third)).isEqualTo("FAILED");
        assertThat(recovery.recoverStalePublications(CUTOFF, 2)).isZero();
    }

    @Test
    void skipsPublicationsLockedByAnotherWorker() throws Exception {
        UUID locked = publication("PUBLISHED", OLD.minusSeconds(1), null, null);
        UUID available = publication("PROCESSING", OLD, null, null);

        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (var statement =
                    connection.prepareStatement("SELECT id FROM event_publication WHERE id = ? FOR UPDATE")) {
                statement.setObject(1, locked);
                statement.executeQuery().close();

                assertThat(recovery.recoverStalePublications(CUTOFF, 1)).isEqualTo(1);
                assertThat(status(locked)).isEqualTo("PUBLISHED");
                assertThat(status(available)).isEqualTo("FAILED");
            } finally {
                connection.rollback();
            }
        }
        assertThat(recovery.recoverStalePublications(CUTOFF, 1)).isEqualTo(1);
        assertThat(status(locked)).isEqualTo("FAILED");
    }

    private UUID publication(String status, Instant published, Instant resubmitted, Instant completed) {
        UUID id = UUID.randomUUID();
        jdbc.sql(
                        """
                        INSERT INTO event_publication
                            (id, listener_id, event_type, serialized_event, publication_date,
                             status, last_resubmission_date, completion_date, completion_attempts)
                        VALUES (:id, 'listener', 'java.lang.String', '"event"', :published,
                                :status, :resubmitted, :completed, 0)
                        """)
                .param("id", id)
                .param("published", Timestamp.from(published))
                .param("status", status)
                .param("resubmitted", resubmitted == null ? null : Timestamp.from(resubmitted), Types.TIMESTAMP)
                .param("completed", completed == null ? null : Timestamp.from(completed), Types.TIMESTAMP)
                .update();
        return id;
    }

    private String status(UUID id) {
        return jdbc.sql("SELECT status FROM event_publication WHERE id = :id")
                .param("id", id)
                .query(String.class)
                .single();
    }
}
