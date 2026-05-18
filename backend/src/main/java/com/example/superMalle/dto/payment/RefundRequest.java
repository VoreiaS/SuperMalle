package com.example.superMalle.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RefundRequest {
    
    @DecimalMin(value = "0.01", message = "Refund amount must be at least 0.01")
    private BigDecimal amount; // null = full refund
    
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason = "requested_by_customer";
}
