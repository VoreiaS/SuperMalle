package com.example.superMalle.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentIntentRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;
    private String paymentMethodType = "card";
}
