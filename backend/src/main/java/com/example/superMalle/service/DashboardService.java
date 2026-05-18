package com.example.superMalle.service;

import com.example.superMalle.dto.admin.*;
import com.example.superMalle.entity.Order;
import com.example.superMalle.entity.enums.OrderStatus;
import org.springframework.data.domain.PageRequest;
import com.example.superMalle.repository.MenuItemRepository;
import com.example.superMalle.repository.OrderItemRepository;
import com.example.superMalle.repository.OrderRepository;
import com.example.superMalle.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;

    public DashboardStatsResponse getDashboardStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);

        long totalOrders = orderRepository.countByCreatedAtBetween(todayStart, todayEnd);
        long pendingOrders = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.PENDING, todayStart, todayEnd);
        long confirmedOrders = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.CONFIRMED, todayStart, todayEnd);
        long preparingOrders = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.PREPARING, todayStart, todayEnd);
        long readyOrders = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.READY, todayStart, todayEnd);
        long outForDeliveryOrders = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.OUT_FOR_DELIVERY, todayStart, todayEnd);
        long deliveredOrders = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.DELIVERED, todayStart, todayEnd);
        long completedOrders = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.COMPLETED, todayStart, todayEnd);
        long cancelledOrders = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.CANCELLED, todayStart, todayEnd);
        BigDecimal todayRevenue = orderRepository.sumCompletedRevenueBetween(todayStart, todayEnd);

        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (completedOrders > 0 && todayRevenue != null) {
            averageOrderValue = todayRevenue.divide(BigDecimal.valueOf(completedOrders), 2, RoundingMode.HALF_UP);
        }

        long totalCustomers = userRepository.count();
        long totalMenuItems = menuItemRepository.count();
        long activeOrders = orderRepository.countActiveOrders();

        // Yesterday's stats for change calculation
        long yesterdayOrders = orderRepository.countByCreatedAtBetween(yesterdayStart, yesterdayEnd);
        BigDecimal yesterdayRevenue = orderRepository.sumCompletedRevenueBetween(yesterdayStart, yesterdayEnd);

        double ordersChange = yesterdayOrders > 0
                ? ((double) totalOrders - yesterdayOrders) / yesterdayOrders * 100.0
                : 0.0;
        double revenueChange = (yesterdayRevenue != null && yesterdayRevenue.compareTo(BigDecimal.ZERO) > 0 && todayRevenue != null)
                ? todayRevenue.subtract(yesterdayRevenue).divide(yesterdayRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        return DashboardStatsResponse.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .confirmedOrders(confirmedOrders)
                .preparingOrders(preparingOrders)
                .readyOrders(readyOrders)
                .outForDeliveryOrders(outForDeliveryOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .totalRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO)
                .todayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO)
                .todayOrders(totalOrders)
                .activeOrders(activeOrders)
                .averageOrderValue(averageOrderValue)
                .totalCustomers(totalCustomers)
                .totalMenuItems(totalMenuItems)
                .ordersChange(Math.round(ordersChange * 100.0) / 100.0)
                .revenueChange(Math.round(revenueChange * 100.0) / 100.0)
                .build();
    }

    public SalesReportResponse getSalesReport(LocalDateTime from, LocalDateTime to) {
        if (from == null) from = LocalDate.now().minusDays(30).atStartOfDay();
        if (to == null) to = LocalDate.now().atTime(LocalTime.MAX);

        if (from.isAfter(to)) {
            LocalDateTime temp = from;
            from = to;
            to = temp;
        }

        BigDecimal totalRevenue = orderRepository.sumCompletedRevenueBetween(from, to);
        long totalOrders = orderRepository.countByCreatedAtBetween(from, to);

        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (totalOrders > 0 && totalRevenue != null) {
            averageOrderValue = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        }

        List<SalesDataPoint> dataPoints = new ArrayList<>();
        LocalDate current = from.toLocalDate();
        LocalDate end = to.toLocalDate();

        int maxDays = 365;
        if (current.plusDays(maxDays - 1).isBefore(end)) {
            end = current.plusDays(maxDays - 1);
        }

        Map<LocalDate, SalesDataPoint> dataMap = new java.util.LinkedHashMap<>();
        LocalDate iter = current;
        while (!iter.isAfter(end)) {
            SalesDataPoint empty = SalesDataPoint.builder()
                    .date(iter.toString())
                    .orderCount(0L)
                    .revenue(BigDecimal.ZERO)
                    .build();
            dataMap.put(iter, empty);
            iter = iter.plusDays(1);
        }

        List<Object[]> dailyResults = orderRepository.findDailySalesReport(from, end.atTime(LocalTime.MAX));
        for (Object[] row : dailyResults) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            long orderCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            BigDecimal revenue = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
            dataMap.put(date, SalesDataPoint.builder()
                    .date(date.toString())
                    .orderCount(orderCount)
                    .revenue(revenue)
                    .build());
        }

        dataPoints.addAll(dataMap.values());

        return SalesReportResponse.builder()
                .dataPoints(dataPoints)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalOrders(totalOrders)
                .averageOrderValue(averageOrderValue)
                .build();
    }

    public List<RecentOrderResponse> getRecentOrders(int limit) {
        if (limit < 1) limit = 10;
        if (limit > 50) limit = 50;
        return orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit))
                .stream()
                .map(order -> RecentOrderResponse.builder()
                        .id(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .customerName(order.getUser() != null ? order.getUser().getName() : null)
                        .userId(order.getUser() != null ? order.getUser().getId() : null)
                        .total(order.getTotalAmount())
                        .status(order.getStatus() != null ? order.getStatus().name() : null)
                        .createdAt(order.getCreatedAt())
                        .build())
                .toList();
    }

    public List<TopItemResponse> getTopSellingItems(int limit) {
        if (limit < 1) limit = 10;
        if (limit > 50) limit = 50;

        List<Object[]> results = orderItemRepository.findTopSellingItems();
        return results.stream()
                .limit(limit)
                .map(row -> TopItemResponse.builder()
                        .menuItemId(row[0] != null ? ((Number) row[0]).longValue() : null)
                        .menuItemName((String) row[1])
                        .totalOrders(row[2] != null ? ((Number) row[2]).longValue() : 0L)
                        .totalRevenue(row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO)
                        .build())
                .toList();
    }
}
