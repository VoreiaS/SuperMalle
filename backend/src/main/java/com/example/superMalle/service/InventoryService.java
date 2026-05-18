package com.example.superMalle.service;

import com.example.superMalle.dto.inventory.InventoryRequest;
import com.example.superMalle.dto.inventory.InventoryResponse;
import com.example.superMalle.dto.inventory.RestockRequest;
import com.example.superMalle.entity.Inventory;
import com.example.superMalle.entity.MenuItem;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.exception.ResourceNotFoundException;
import com.example.superMalle.repository.InventoryRepository;
import com.example.superMalle.repository.MenuItemRepository;
import com.example.superMalle.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Value("${app.admin.email:admin@supermalle.com}")
    private String adminEmail;

    @Transactional
    public InventoryResponse createInventory(InventoryRequest request) {
        if (request == null) {
            throw new BadRequestException("Inventory request cannot be null");
        }

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", request.getMenuItemId()));

        if (inventoryRepository.existsByMenuItem(menuItem)) {
            throw new BadRequestException("Inventory already exists for this menu item");
        }

        Inventory inventory = Inventory.builder()
                .menuItem(menuItem)
                .quantity(request.getQuantity())
                .reorderLevel(request.getReorderLevel() != null ? request.getReorderLevel() : 10)
                .maxQuantity(request.getMaxQuantity())
                .unit(request.getUnit())
                .costPerUnit(request.getCostPerUnit())
                .supplierName(request.getSupplierName())
                .supplierContact(request.getSupplierContact())
                .nextRestockDate(request.getNextRestockDate())
                .notes(request.getNotes())
                .isActive(true)
                .createdBy(getAuthenticatedUserEmail())
                .build();

        inventory = inventoryRepository.save(inventory);
        log.info("Created inventory for menu item: {}", menuItem.getName());

        return toResponse(inventory);
    }

    @Transactional
    public InventoryResponse updateInventory(Long id, InventoryRequest request) {
        if (id == null) {
            throw new BadRequestException("Inventory ID is required");
        }
        if (request == null) {
            throw new BadRequestException("Inventory request cannot be null");
        }

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));

        if (request.getMenuItemId() != null && !request.getMenuItemId().equals(inventory.getMenuItem().getId())) {
            MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", request.getMenuItemId()));
            if (inventoryRepository.existsByMenuItem(menuItem)) {
                throw new BadRequestException("Inventory already exists for this menu item");
            }
            inventory.setMenuItem(menuItem);
        }

        if (request.getQuantity() != null) {
            inventory.setQuantity(request.getQuantity());
        }
        if (request.getReorderLevel() != null) {
            inventory.setReorderLevel(request.getReorderLevel());
        }
        if (request.getMaxQuantity() != null) {
            inventory.setMaxQuantity(request.getMaxQuantity());
        }
        if (request.getUnit() != null) {
            inventory.setUnit(request.getUnit());
        }
        if (request.getCostPerUnit() != null) {
            inventory.setCostPerUnit(request.getCostPerUnit());
        }
        if (request.getSupplierName() != null) {
            inventory.setSupplierName(request.getSupplierName());
        }
        if (request.getSupplierContact() != null) {
            inventory.setSupplierContact(request.getSupplierContact());
        }
        if (request.getNextRestockDate() != null) {
            inventory.setNextRestockDate(request.getNextRestockDate());
        }
        if (request.getNotes() != null) {
            inventory.setNotes(request.getNotes());
        }

        inventory.setUpdatedBy(getAuthenticatedUserEmail());
        inventory = inventoryRepository.save(inventory);
        log.info("Updated inventory for menu item: {}", inventory.getMenuItem().getName());

        return toResponse(inventory);
    }

    @Transactional
    public InventoryResponse restockInventory(Long id, RestockRequest request) {
        if (id == null) {
            throw new BadRequestException("Inventory ID is required");
        }
        if (request == null || request.getQuantityToAdd() == null) {
            throw new BadRequestException("Quantity to add is required");
        }

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));

        if (request.getQuantityToAdd() <= 0) {
            throw new BadRequestException("Quantity to add must be positive");
        }

        Integer oldQuantity = inventory.getQuantity();
        inventory.setQuantity(oldQuantity + request.getQuantityToAdd());
        inventory.setLastRestockedAt(LocalDateTime.now());
        if (request.getNextRestockDate() != null) {
            inventory.setNextRestockDate(request.getNextRestockDate());
        }
        if (request.getNotes() != null) {
            inventory.setNotes(request.getNotes());
        }
        inventory.setUpdatedBy(getAuthenticatedUserEmail());
        inventory = inventoryRepository.save(inventory);

        log.info("Restocked inventory for menu item: {}, added {} units ({} -> {})",
                inventory.getMenuItem().getName(), request.getQuantityToAdd(), oldQuantity, inventory.getQuantity());

        // Notify if item was out of stock
        if (oldQuantity <= 0 && inventory.getQuantity() > 0) {
            notificationService.notifyInventoryRestock(inventory.getId(), inventory.getMenuItem().getName(), inventory.getQuantity());
        }

        if (inventory.isLowStock()) {
            try {
                emailService.sendLowStockAlert(
                        adminEmail,
                        inventory.getMenuItem().getName(),
                        inventory.getQuantity(),
                        inventory.getReorderLevel()
                );
            } catch (Exception e) {
                log.warn("Failed to send low stock alert for {}: {}", inventory.getMenuItem().getName(), e.getMessage());
            }
        }

        return toResponse(inventory);
    }

    @Transactional
    public void deleteInventory(Long id) {
        if (id == null) {
            throw new BadRequestException("Inventory ID is required");
        }

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));

        inventory.setIsActive(false);
        inventory.setUpdatedBy(getAuthenticatedUserEmail());
        inventoryRepository.save(inventory);

        log.info("Deleted inventory for menu item: {}", inventory.getMenuItem().getName());
    }

    public InventoryResponse getInventoryById(Long id) {
        if (id == null) {
            throw new BadRequestException("Inventory ID is required");
        }

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));

        return toResponse(inventory);
    }

    public InventoryResponse getInventoryByMenuItemId(Long menuItemId) {
        if (menuItemId == null) {
            throw new BadRequestException("Menu item ID is required");
        }

        Inventory inventory = inventoryRepository.findByMenuItemId(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "menuItemId", menuItemId));

        return toResponse(inventory);
    }

    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<InventoryResponse> getLowStockItems() {
        return inventoryRepository.findLowStockItems().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<InventoryResponse> getOutOfStockItems() {
        return inventoryRepository.findOutOfStockItems().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<InventoryResponse> getOverstockedItems() {
        return inventoryRepository.findOverstockedItems().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<InventoryResponse> getItemsNeedingRestock() {
        return inventoryRepository.findItemsNeedingRestock(LocalDateTime.now()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Long countLowStockItems() {
        return inventoryRepository.countLowStockItems();
    }

    public Long countOutOfStockItems() {
        return inventoryRepository.countOutOfStockItems();
    }

    private InventoryResponse toResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .menuItemId(inventory.getMenuItem() != null ? inventory.getMenuItem().getId() : null)
                .menuItemName(inventory.getMenuItem() != null ? inventory.getMenuItem().getName() : null)
                .quantity(inventory.getQuantity())
                .reorderLevel(inventory.getReorderLevel())
                .maxQuantity(inventory.getMaxQuantity())
                .unit(inventory.getUnit())
                .costPerUnit(inventory.getCostPerUnit())
                .supplierName(inventory.getSupplierName())
                .supplierContact(inventory.getSupplierContact())
                .lastRestockedAt(inventory.getLastRestockedAt())
                .nextRestockDate(inventory.getNextRestockDate())
                .isActive(inventory.getIsActive())
                .notes(inventory.getNotes())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .createdBy(inventory.getCreatedBy())
                .updatedBy(inventory.getUpdatedBy())
                .isLowStock(inventory.isLowStock())
                .isOutOfStock(inventory.isOutOfStock())
                .isOverstocked(inventory.isOverstocked())
                .quantityToReorder(inventory.isLowStock() ? (inventory.getReorderLevel() - inventory.getQuantity()) : 0)
                .build();
    }

    private String getAuthenticatedUserEmail() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getEmail();
            }
        } catch (Exception e) {
            log.warn("Could not get authenticated user email", e);
        }
        return "system";
    }
}
