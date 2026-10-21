package com.example.outboxpattern.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

class JdbcOutboxStatsServiceTest {

    @SuppressWarnings("unchecked")
    @Test
    void queriesPendingAndFailedRecords() {
        JdbcClient jdbcClient = mock(JdbcClient.class);

        JdbcClient.StatementSpec pendingSpec = mock(JdbcClient.StatementSpec.class);
        JdbcClient.MappedQuerySpec<Long> pendingQuery = mock(JdbcClient.MappedQuerySpec.class);
        when(jdbcClient.sql(JdbcOutboxStatsService.PENDING_QUERY)).thenReturn(pendingSpec);
        when(pendingSpec.query(Long.class)).thenReturn(pendingQuery);
        when(pendingQuery.single()).thenReturn(42L);

        JdbcClient.StatementSpec failedSpec = mock(JdbcClient.StatementSpec.class);
        JdbcClient.MappedQuerySpec<Long> failedQuery = mock(JdbcClient.MappedQuerySpec.class);
        when(jdbcClient.sql(JdbcOutboxStatsService.FAILED_QUERY)).thenReturn(failedSpec);
        when(failedSpec.query(Long.class)).thenReturn(failedQuery);
        when(failedQuery.single()).thenReturn(7L);

        JdbcOutboxStatsService service = new JdbcOutboxStatsService(jdbcClient);
        OutboxStats stats = service.getStats();

        assertThat(stats.pendingCount()).isEqualTo(42L);
        assertThat(stats.failedCount()).isEqualTo(7L);
    }
}
