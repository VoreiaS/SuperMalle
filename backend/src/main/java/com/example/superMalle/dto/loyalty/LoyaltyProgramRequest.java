package com.example.superMalle.dto.loyalty;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyProgramRequest {

    @NotBlank(message = "Program name is required")
    @Size(max = 100, message = "Program name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Points per dollar is required")
    @Min(value = 1, message = "Points per dollar must be at least 1")
    private Integer pointsPerDollar;

    @NotNull(message = "Redemption rate is required")
    @Min(value = 1, message = "Redemption rate must be at least 1")
    private Integer redemptionRate;

    @NotNull(message = "Minimum points to redeem is required")
    @Min(value = 0, message = "Minimum points to redeem cannot be negative")
    private Integer minPointsToRedeem;

    @Min(value = 0, message = "Max points per order cannot be negative")
    private Integer maxPointsPerOrder;

    @Min(value = 0, message = "Welcome bonus points cannot be negative")
    private Integer welcomeBonusPoints;

    @Min(value = 0, message = "Referral bonus points cannot be negative")
    private Integer referralBonusPoints;
}
