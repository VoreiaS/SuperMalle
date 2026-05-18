package com.example.superMalle.dto.loyalty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoyaltyResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long loyaltyProgramId;
    private String loyaltyProgramName;
    private Integer totalPoints;
    private Integer availablePoints;
    private Integer redeemedPoints;
    private String tierLevel;
    private Integer lifetimePoints;
    private Integer totalOrders;
    private Double totalSpent;
    private LocalDateTime lastOrderDate;
    private String referralCode;
    private String referredBy;
    private Integer referralCount;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields
    private Double tierMultiplier;
    private String tierBenefits;
    private Integer pointsToNextTier;
    private String nextTier;
    private Double discountValue; // Current discount value based on available points
}
