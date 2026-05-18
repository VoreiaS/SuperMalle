package com.example.superMalle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_loyalty")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoyalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loyalty_program_id", nullable = false)
    private LoyaltyProgram loyaltyProgram;

    @Column(name = "total_points", nullable = false)
    @Builder.Default
    private Integer totalPoints = 0;

    @Column(name = "available_points", nullable = false)
    @Builder.Default
    private Integer availablePoints = 0;

    @Column(name = "redeemed_points", nullable = false)
    @Builder.Default
    private Integer redeemedPoints = 0;

    @Column(name = "tier_level", nullable = false)
    @Builder.Default
    private String tierLevel = "BRONZE";

    @Column(name = "lifetime_points", nullable = false)
    @Builder.Default
    private Integer lifetimePoints = 0;

    @Column(name = "total_orders", nullable = false)
    @Builder.Default
    private Integer totalOrders = 0;

    @Column(name = "total_spent", nullable = false)
    @Builder.Default
    private Double totalSpent = 0.0;

    @Column(name = "last_order_date")
    private LocalDateTime lastOrderDate;

    @Column(name = "points_expire_at")
    private LocalDateTime pointsExpireAt;

    @Builder.Default
    @Column(name = "points_expired")
    private Integer pointsExpired = 0;

    @Column(name = "referral_code")
    private String referralCode;

    @Column(name = "referred_by")
    private String referredBy;

    @Column(name = "referral_count", nullable = false)
    @Builder.Default
    private Integer referralCount = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Add points to user's balance
     */
    public void addPoints(Integer points) {
        if (points != null && points > 0) {
            this.totalPoints += points;
            this.availablePoints += points;
            this.lifetimePoints += points;
            updateTierLevel();
        }
    }

    /**
     * Redeem points from user's balance
     */
    public void redeemPoints(Integer points) {
        if (points != null && points > 0 && this.availablePoints >= points) {
            this.availablePoints -= points;
            this.redeemedPoints += points;
        }
    }

    /**
     * Update tier level based on lifetime points
     */
    private void updateTierLevel() {
        if (lifetimePoints >= 10000) {
            this.tierLevel = "PLATINUM";
        } else if (lifetimePoints >= 5000) {
            this.tierLevel = "GOLD";
        } else if (lifetimePoints >= 1000) {
            this.tierLevel = "SILVER";
        } else {
            this.tierLevel = "BRONZE";
        }
    }

    /**
     * Check if user can redeem points
     */
    public boolean canRedeemPoints(Integer points) {
        return points != null && points > 0 && this.availablePoints >= points;
    }

    /**
     * Get tier multiplier for bonus points
     */
    public Double getTierMultiplier() {
        return switch (tierLevel) {
            case "PLATINUM" -> 1.5;
            case "GOLD" -> 1.25;
            case "SILVER" -> 1.1;
            default -> 1.0;
        };
    }
}
