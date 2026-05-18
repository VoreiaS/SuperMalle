package com.example.superMalle.dto.order;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderModificationRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotBlank(message = "Modification type is required")
    private String modificationType; // ADD_ITEM, REMOVE_ITEM, UPDATE_QUANTITY, UPDATE_ADDRESS, CANCEL_ITEM

    private String previousValue;

    private String newValue;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
}
