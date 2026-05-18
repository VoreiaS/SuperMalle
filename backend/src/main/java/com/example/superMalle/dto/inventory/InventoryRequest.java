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
public class InventoryRequest {

    @NotNull(message = "Menu item ID is required")
    private Long menuItemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Min(value = 0, message = "Reorder level cannot be negative")
    private Integer reorderLevel;

    @Min(value = 0, message = "Max quantity cannot be negative")
    private Integer maxQuantity;

    @NotBlank(message = "Unit is required")
    private String unit;

    @DecimalMin(value = "0.0", message = "Cost per unit cannot be negative")
    private Double costPerUnit;

    private String supplierName;

    private String supplierContact;

    private LocalDateTime nextRestockDate;

    private String notes;
}
