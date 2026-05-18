package com.example.superMalle.controller;

import com.example.superMalle.dto.menu.PagedResponse;
import com.example.superMalle.dto.order.*;
import com.example.superMalle.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(request));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getMyOrders(page, size));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/{id:\\d+}/status")
    public ResponseEntity<OrderResponse> getOrderStatus(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderStatus(id));
    }

    @PostMapping("/{id:\\d+}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id,
                                             @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        orderService.cancelOrder(id, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id:\\d+}/review")
    public ResponseEntity<ReviewResponse> addReview(@PathVariable Long id,
                                                     @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(orderService.addReview(id, request));
    }
}
