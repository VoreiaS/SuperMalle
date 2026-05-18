package com.example.superMalle.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private String orderType;
    private String status;
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String deliveryAddress;
    private BigDecimal deliveryCharge;
    private String specialInstructions;
    private String couponCode;
    private BigDecimal discountAmount;
    private BigDecimal tipAmount;
    private String cancellationReason;
    private LocalDateTime estimatedReadyAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;
    private List<OrderStatusLogResponse> statusLog;
}
