package com.example.superMalle.dto.menu;

import com.example.superMalle.entity.enums.TaxCategory;
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
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
    private String imageUrl;
    private Boolean isAvailable;
    private Integer preparationTimeMinutes;
    private String customizations;
    private TaxCategory taxCategory;
    private List<OptionGroupResponse> optionGroups;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
