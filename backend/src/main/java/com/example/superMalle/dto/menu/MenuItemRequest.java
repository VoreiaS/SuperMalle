package com.example.superMalle.dto.menu;

import com.example.superMalle.entity.enums.TaxCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MenuItemRequest {
    @NotBlank(message = "Item name is required")
    private String name;
    private String description;
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    private String imageUrl;
    private Boolean isAvailable;
    private Integer preparationTimeMinutes;
    private String customizations;
    private TaxCategory taxCategory;
}
