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
public class DashboardStatsResponse {
    private long totalOrders;
    private long pendingOrders;
    private long confirmedOrders;
    private long preparingOrders;
    private long readyOrders;
    private long outForDeliveryOrders;
    private long deliveredOrders;
    private long completedOrders;
    private long cancelledOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private long totalCustomers;
    private long totalMenuItems;
    private BigDecimal todayRevenue;
    private long todayOrders;
    private long activeOrders;
    private double ordersChange;
    private double revenueChange;
}
