package com.example.superMalle.controller.admin;

import com.example.superMalle.dto.menu.PagedResponse;
import com.example.superMalle.dto.order.*;
import com.example.superMalle.entity.enums.OrderStatus;
import com.example.superMalle.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<PagedResponse<OrderResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getAllOrders(status, from, to, page, size));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderDetails(id));
    }

    @PutMapping("/{id:\\d+}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long id,
                                                           @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }

    @PostMapping("/{id:\\d+}/cancel")
    public ResponseEntity<OrderResponse> adminCancelOrder(@PathVariable Long id,
                                                          @RequestBody AdminCancelOrderRequest request) {
        return ResponseEntity.ok(orderService.adminCancelOrder(id, request));
    }

    @PutMapping("/{id:\\d+}/eta")
    public ResponseEntity<OrderResponse> updateEstimatedReadyAt(@PathVariable Long id,
                                                                 @Valid @RequestBody UpdateEtaRequest request) {
        return ResponseEntity.ok(orderService.updateEstimatedReadyAt(id, request));
    }
}
