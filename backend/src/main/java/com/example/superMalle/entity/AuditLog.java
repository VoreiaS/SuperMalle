package com.example.superMalle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Audit Log Entity
 * 
 * Tracks all system operations for compliance and security
 * Records user actions, system events, and data changes
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_entity_type", columnList = "entity_type"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_correlation_id", columnList = "correlation_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = true)
    private Long userId;

    @Column(name = "username", nullable = true, length = 100)
    private String username;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "entity_type", nullable = true, length = 100)
    private String entityType;

    @Column(name = "entity_id", nullable = true, length = 100)
    private String entityId;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "ip_address", nullable = true, length = 50)
    private String ipAddress;

    @Column(name = "user_agent", nullable = true, length = 500)
    private String userAgent;

    @Column(name = "request_method", nullable = true, length = 10)
    private String requestMethod;

    @Column(name = "request_uri", nullable = true, length = 500)
    private String requestUri;

    @Column(name = "old_value", nullable = true, columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", nullable = true, columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "correlation_id", nullable = true, length = 100)
    private String correlationId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "error_message", nullable = true, columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "duration_ms", nullable = true)
    private Long durationMs;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (status == null) {
            status = "SUCCESS";
        }
    }
}
