package com.example.superMalle.task;

import com.example.superMalle.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled task to clean up expired idempotency records.
 * 
 * Runs daily at 3 AM to prevent unbounded table growth.
 * Uses @Transactional to ensure atomic deletion.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyCleanupTask {

    private final IdempotencyKeyRepository idempotencyKeyRepository;

    /**
     * Delete expired idempotency keys daily.
     * Cron: 0 0 3 * * ? = Every day at 3:00 AM
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredKeys() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
            int deleted = idempotencyKeyRepository.deleteExpired(cutoff);
            if (deleted > 0) {
                log.info("Cleaned up {} expired idempotency records", deleted);
            }
        } catch (Exception e) {
            log.error("Failed to cleanup expired idempotency keys: {}", e.getMessage(), e);
            // Don't rethrow - cleanup is best-effort, failure shouldn't crash app
        }
    }
}
