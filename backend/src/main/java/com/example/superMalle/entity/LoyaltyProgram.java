package com.example.superMalle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_program")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "points_per_dollar", nullable = false)
    @Builder.Default
    private Integer pointsPerDollar = 10;

    @Column(name = "redemption_rate", nullable = false)
    @Builder.Default
    private Integer redemptionRate = 100; // 100 points = $1 discount

    @Column(name = "min_points_to_redeem", nullable = false)
    @Builder.Default
    private Integer minPointsToRedeem = 500;

    @Column(name = "max_points_per_order")
    private Integer maxPointsPerOrder;

    @Column(name = "welcome_bonus_points")
    @Builder.Default
    private Integer welcomeBonusPoints = 100;

    @Column(name = "referral_bonus_points")
    @Builder.Default
    private Integer referralBonusPoints = 500;

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
     * Calculate points earned from order amount
     */
    public Integer calculatePointsEarned(Double orderAmount) {
        if (orderAmount == null || orderAmount <= 0) {
            return 0;
        }
        return (int) (orderAmount * pointsPerDollar);
    }

    /**
     * Calculate discount value from points
     */
    public Double calculateDiscountValue(Integer points) {
        if (points == null || points < minPointsToRedeem) {
            return 0.0;
        }
        return (double) points / redemptionRate;
    }
}
