package com.example.superMalle.dto.inventory;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestockRequest {

    @NotNull(message = "Quantity to add is required")
    @Min(value = 1, message = "Quantity to add must be at least 1")
    private Integer quantityToAdd;

    private LocalDateTime nextRestockDate;

    private String notes;
}
