package com.example.superMalle.dto.order;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminCancelOrderRequest {
    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    private String reason;
}
