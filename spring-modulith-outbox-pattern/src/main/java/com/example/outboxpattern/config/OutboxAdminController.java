package com.example.outboxpattern.config;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/outbox")
public class OutboxAdminController {

    private final OutboxStatsService statsService;

    /** Creates an admin endpoint backed by the supplied outbox statistics service. */
    public OutboxAdminController(OutboxStatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * Returns pending and unresolved failed outbox counts with the current timestamp.
     *
     * @return HTTP 200 with {@code pendingCount}, {@code failedCount}, and an {@link Instant}
     *     under {@code timestamp}
     * @throws RuntimeException if statistics retrieval fails
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        OutboxStats stats = statsService.getStats();

        return ResponseEntity.ok(Map.of(
                "pendingCount", stats.pendingCount(),
                "failedCount", stats.failedCount(),
                "timestamp", Instant.now()));
    }
}
