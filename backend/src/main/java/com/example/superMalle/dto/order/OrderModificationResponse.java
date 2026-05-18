package com.example.superMalle.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderModificationResponse {

    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String userName;
    private String modificationType;
    private String previousValue;
    private String newValue;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED, COMPLETED
    private Double priceAdjustment;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String rejectedBy;
    private LocalDateTime rejectedAt;
    private String rejectedReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields
    private Boolean canBeApproved;
    private Boolean canBeRejected;
    private String statusDisplay;
}
