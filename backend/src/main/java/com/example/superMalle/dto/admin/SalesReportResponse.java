package com.example.superMalle.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportResponse {
    private List<SalesDataPoint> dataPoints;
    private BigDecimal totalRevenue;
    private long totalOrders;
    private BigDecimal averageOrderValue;
}
