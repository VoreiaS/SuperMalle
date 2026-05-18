package com.example.superMalle.controller.admin;

import com.example.superMalle.dto.admin.DashboardStatsResponse;
import com.example.superMalle.dto.admin.RecentOrderResponse;
import com.example.superMalle.dto.admin.SalesReportResponse;
import com.example.superMalle.dto.admin.TopItemResponse;
import com.example.superMalle.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/charts")
    public ResponseEntity<SalesReportResponse> getSalesChart(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(dashboardService.getSalesReport(from, to));
    }

    @GetMapping("/recent-orders")
    public ResponseEntity<java.util.List<RecentOrderResponse>> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentOrders(limit));
    }

    @GetMapping("/top-items")
    public ResponseEntity<java.util.List<TopItemResponse>> getTopSellingItems(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getTopSellingItems(limit));
    }
}
