package com.example.superMalle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Feature Flag Entity
 * 
 * Manages feature flags for controlled feature rollouts
 * Supports percentage-based rollouts and user targeting
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Entity
@Table(name = "feature_flags", indexes = {
    @Index(name = "idx_feature_flag_name", columnList = "name", unique = true),
    @Index(name = "idx_feature_flag_enabled", columnList = "enabled")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "rollout_percentage", nullable = false)
    private Integer rolloutPercentage;

    @Column(name = "target_user_ids", columnDefinition = "TEXT")
    private String targetUserIds;

    @Column(name = "target_roles", columnDefinition = "TEXT")
    private String targetRoles;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = true, length = 100)
    private String createdBy;

    @Column(name = "updated_by", nullable = true, length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (enabled == null) {
            enabled = false;
        }
        if (rolloutPercentage == null) {
            rolloutPercentage = 100;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
