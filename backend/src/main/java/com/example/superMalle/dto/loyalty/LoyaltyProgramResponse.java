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
public class LoyaltyProgramResponse {

    private Long id;
    private String name;
    private String description;
    private Integer pointsPerDollar;
    private Integer redemptionRate;
    private Integer minPointsToRedeem;
    private Integer maxPointsPerOrder;
    private Integer welcomeBonusPoints;
    private Integer referralBonusPoints;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields
    private String redemptionRateDisplay; // e.g., "100 points = $1"
}
