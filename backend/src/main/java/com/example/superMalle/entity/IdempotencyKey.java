package com.example.superMalle.entity;

import com.example.superMalle.entity.enums.IdempotencyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * Idempotency key tracking entity for preventing duplicate request processing.
 * 
 * Security design:
 * - Unique index on (key, entity, userId) prevents cross-user replay attacks
 * - request_hash ensures same key with different payload is rejected
 * - pessimistic locking in repository prevents race conditions
 * - TTL-based cleanup prevents unbounded table growth
 * - userId scoping ensures users cannot replay each other's requests
 */
@Entity
@Table(name = "idempotency_keys", indexes = {
    @Index(name = "idx_idempotency_lookup", columnList = "key, entity, user_id", unique = true),
    @Index(name = "idx_idempotency_cleanup", columnList = "expires_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class IdempotencyKey {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(nullable = false, length = 255)
    private String key;

    @EqualsAndHashCode.Include
    @Column(nullable = false, length = 50)
    private String entity;

    /** User ID scoping: prevents cross-user replay attacks */
    @Column(name = "user_id")
    private Long userId;

    /** SHA-256 hash of request payload - ensures same key with different body is rejected */
    @Column(name = "request_hash", length = 64, nullable = false)
    private String requestHash;

    /** Cached JSON response for successful requests (replayed on duplicate) */
    @Column(columnDefinition = "TEXT")
    private String responseBody;

    /** HTTP status code of cached response */
    @Column(name = "response_status")
    private Integer responseStatus;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)", nullable = false)
    @Builder.Default
    private IdempotencyStatus status = IdempotencyStatus.PROCESSING;

    /** Error message if status is FAILED */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /** TTL expiry - after this, key can be reused (prevents unbounded growth) */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isExpired() { 
        return LocalDateTime.now().isAfter(expiresAt); 
    }
    
    public boolean isCompleted() { 
        return status == IdempotencyStatus.COMPLETED && !isExpired(); 
    }
    
    public boolean payloadMatches(String hash) { 
        return requestHash != null && requestHash.equals(hash); 
    }
}
