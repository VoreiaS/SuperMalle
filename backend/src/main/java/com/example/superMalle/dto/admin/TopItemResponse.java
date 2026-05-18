package com.example.superMalle.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopItemResponse {
    private Long menuItemId;
    private String menuItemName;
    private long totalOrders;
    private BigDecimal totalRevenue;
}
