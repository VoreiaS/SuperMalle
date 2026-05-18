package com.example.superMalle.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionResponse {
    private Long id;
    private Long optionGroupId;
    private String name;
    private BigDecimal priceModifier;
    private Boolean isDefault;
    private Integer sortOrder;
    private Boolean isActive;
}
