package com.example.superMalle.entity;

import com.example.superMalle.entity.enums.DiscountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    @Pattern(regexp = "^[A-Z0-9_-]{3,20}$", message = "Code must be 3-20 chars, uppercase letters/numbers/-/_")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)", nullable = false)
    @Builder.Default
    private DiscountType discountType = DiscountType.FIXED;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 4)
    private BigDecimal value;

    @Column(precision = 12, scale = 4)
    private BigDecimal minOrderAmount;

    @Column(precision = 12, scale = 4)
    private BigDecimal maxDiscountAmount;

    private Integer usageLimit;

    @Builder.Default
    private Integer usageCount = 0;

    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime expiresAt;

    @Builder.Default
    private Boolean deleted = false;

    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }

    @PrePersist
    @PreUpdate
    public void handleSoftDelete() {
        if (Boolean.TRUE.equals(deleted) && deletedAt == null) {
            deletedAt = LocalDateTime.now();
        }
    }

    /**
     * Calculate discount amount for a given subtotal.
     * Security: Validates discount doesn't exceed subtotal or configured max.
     */
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (!Boolean.TRUE.equals(isActive) || isDeleted()) {
            return BigDecimal.ZERO;
        }
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            return BigDecimal.ZERO;
        }
        if (minOrderAmount != null && subtotal.compareTo(minOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (discountType == DiscountType.PERCENTAGE) {
            discount = subtotal.multiply(value)
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
                discount = maxDiscountAmount;
            }
        } else {
            discount = value;
        }

        // Security: Never discount more than the subtotal
        return discount.min(subtotal);
    }

    /**
     * Check if coupon can be applied (usage limits, expiry, etc.)
     */
    public boolean isApplicable() {
        return Boolean.TRUE.equals(isActive)
                && !isDeleted()
                && (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()))
                && (usageLimit == null || usageCount < usageLimit);
    }
}
