package com.example.superMalle.repository;

import com.example.superMalle.entity.IdempotencyKey;
import com.example.superMalle.entity.enums.IdempotencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for idempotency key tracking with concurrency-safe operations.
 * 
 * Security: Uses pessimistic locking (PESSIMISTIC_WRITE) to prevent race conditions
 * when multiple requests with the same key arrive simultaneously.
 */
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    /**
     * Find idempotency record with pessimistic write lock.
     * This prevents concurrent requests from both seeing "no record" and proceeding.
     * 
     * @param key The idempotency key
     * @param entity The logical entity name (e.g., "payment", "order")
     * @param userId The user ID for scoping (prevents cross-user replay)
     * @return Optional containing the locked record if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ik FROM IdempotencyKey ik WHERE ik.key = :key AND ik.entity = :entity AND ik.userId = :userId")
    Optional<IdempotencyKey> findByKeyAndEntityAndUserIdWithLock(
            @Param("key") String key,
            @Param("entity") String entity,
            @Param("userId") Long userId
    );

    /**
     * Find without lock for read-only checks (use only when race condition is acceptable).
     */
    Optional<IdempotencyKey> findByKeyAndEntityAndUserId(
            @Param("key") String key,
            @Param("entity") String entity,
            @Param("userId") Long userId
    );

    /**
     * Cleanup expired records - call periodically via scheduled task.
     * @param before Expiry threshold
     * @return Number of records deleted
     */
    @Query("DELETE FROM IdempotencyKey ik WHERE ik.expiresAt < :before")
    int deleteExpired(@Param("before") LocalDateTime before);

    /**
     * Count records by status for monitoring/audit.
     */
    long countByEntityAndStatus(@Param("entity") String entity, @Param("status") IdempotencyStatus status);
}
