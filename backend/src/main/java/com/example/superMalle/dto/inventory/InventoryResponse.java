package com.example.superMalle.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    private Long menuItemId;
    private String menuItemName;
    private Integer quantity;
    private Integer reorderLevel;
    private Integer maxQuantity;
    private String unit;
    private Double costPerUnit;
    private String supplierName;
    private String supplierContact;
    private LocalDateTime lastRestockedAt;
    private LocalDateTime nextRestockDate;
    private Boolean isActive;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Computed fields
    private Boolean isLowStock;
    private Boolean isOutOfStock;
    private Boolean isOverstocked;
    private Integer quantityToReorder;
}
