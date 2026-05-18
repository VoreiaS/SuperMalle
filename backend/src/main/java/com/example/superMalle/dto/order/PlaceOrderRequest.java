package com.example.superMalle.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceOrderRequest {
    @NotNull(message = "Order type is required")
    private String orderType; // DELIVERY or PICKUP
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // card, cash, etc.
    
    private String deliveryAddress;
    private String specialInstructions;
    private String couponCode;
    private java.math.BigDecimal tipAmount;
}
