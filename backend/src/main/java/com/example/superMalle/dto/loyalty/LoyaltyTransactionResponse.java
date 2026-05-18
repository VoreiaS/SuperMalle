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
public class LoyaltyTransactionResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long loyaltyProgramId;
    private String loyaltyProgramName;
    private Long orderId;
    private String orderNumber;
    private String transactionType;
    private Integer points;
    private Integer balanceBefore;
    private Integer balanceAfter;
    private String description;
    private String referenceId;
    private LocalDateTime createdAt;

    // Computed fields
    private Boolean isPositive; // true for earned points, false for redeemed
}
