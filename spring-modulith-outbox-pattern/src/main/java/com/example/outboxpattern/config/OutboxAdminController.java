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

    public OutboxAdminController(OutboxStatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        OutboxStats stats = statsService.getStats();

        return ResponseEntity.ok(Map.of(
                "pendingCount", stats.pendingCount(),
                "failedCount", stats.failedCount(),
                "timestamp", Instant.now()));
    }
}
