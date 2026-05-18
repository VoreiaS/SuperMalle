package com.example.superMalle.controller;

import com.example.superMalle.dto.inventory.InventoryRequest;
import com.example.superMalle.dto.inventory.InventoryResponse;
import com.example.superMalle.dto.inventory.RestockRequest;
import com.example.superMalle.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.createInventory(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.updateInventory(id, request));
    }

    @PostMapping("/{id}/restock")
    public ResponseEntity<InventoryResponse> restockInventory(
            @PathVariable Long id,
            @Valid @RequestBody RestockRequest request) {
        return ResponseEntity.ok(inventoryService.restockInventory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    @GetMapping("/menu-item/{menuItemId}")
    public ResponseEntity<InventoryResponse> getInventoryByMenuItemId(@PathVariable Long menuItemId) {
        return ResponseEntity.ok(inventoryService.getInventoryByMenuItemId(menuItemId));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryResponse>> getLowStockItems() {
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<InventoryResponse>> getOutOfStockItems() {
        return ResponseEntity.ok(inventoryService.getOutOfStockItems());
    }

    @GetMapping("/overstocked")
    public ResponseEntity<List<InventoryResponse>> getOverstockedItems() {
        return ResponseEntity.ok(inventoryService.getOverstockedItems());
    }

    @GetMapping("/needs-restock")
    public ResponseEntity<List<InventoryResponse>> getItemsNeedingRestock() {
        return ResponseEntity.ok(inventoryService.getItemsNeedingRestock());
    }

    @GetMapping("/stats/low-stock-count")
    public ResponseEntity<Long> countLowStockItems() {
        return ResponseEntity.ok(inventoryService.countLowStockItems());
    }

    @GetMapping("/stats/out-of-stock-count")
    public ResponseEntity<Long> countOutOfStockItems() {
        return ResponseEntity.ok(inventoryService.countOutOfStockItems());
    }
}
