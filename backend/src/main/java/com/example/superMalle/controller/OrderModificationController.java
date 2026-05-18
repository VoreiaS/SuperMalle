package com.example.superMalle.controller;

import com.example.superMalle.dto.order.*;
import com.example.superMalle.service.OrderModificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-modifications")
@RequiredArgsConstructor
public class OrderModificationController {

    private final OrderModificationService orderModificationService;

    // === Customer Endpoints ===

    @PostMapping
    public ResponseEntity<OrderModificationResponse> requestModification(@Valid @RequestBody OrderModificationRequest request) {
        return ResponseEntity.ok(orderModificationService.requestModification(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderModificationResponse>> getMyModifications() {
        return ResponseEntity.ok(orderModificationService.getMyModifications());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderModificationResponse>> getOrderModifications(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderModificationService.getOrderModifications(orderId));
    }

    // === Admin Endpoints ===

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderModificationResponse>> getAllPendingModifications() {
        return ResponseEntity.ok(orderModificationService.getAllPendingModifications());
    }

    @GetMapping("/stats/pending-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> countPendingModifications() {
        return ResponseEntity.ok(orderModificationService.countPendingModifications());
    }

    @PostMapping("/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderModificationResponse> approveModification(@Valid @RequestBody ApproveModificationRequest request) {
        return ResponseEntity.ok(orderModificationService.approveModification(request));
    }

    @PostMapping("/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderModificationResponse> rejectModification(@Valid @RequestBody RejectModificationRequest request) {
        return ResponseEntity.ok(orderModificationService.rejectModification(request));
    }
}
