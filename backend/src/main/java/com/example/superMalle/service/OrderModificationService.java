package com.example.superMalle.service;

import com.example.superMalle.dto.order.*;
import com.example.superMalle.entity.*;
import com.example.superMalle.entity.enums.OrderStatus;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.exception.ResourceNotFoundException;
import com.example.superMalle.repository.*;
import com.example.superMalle.security.CustomUserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderModificationService {

    private final OrderModificationRepository orderModificationRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // === Customer Methods ===

    @Transactional
    public OrderModificationResponse requestModification(OrderModificationRequest request) {
        if (request == null) {
            throw new BadRequestException("Modification request cannot be null");
        }
        if (request.getOrderId() == null) {
            throw new BadRequestException("Order ID is required");
        }
        if (request.getModificationType() == null || request.getModificationType().isBlank()) {
            throw new BadRequestException("Modification type is required");
        }

        Long userId = getAuthenticatedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Order order = orderRepository.findByIdAndUserId(request.getOrderId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        // Validate order can be modified
        if (!canOrderBeModified(order)) {
            throw new BadRequestException("Order cannot be modified at this stage");
        }

        // Validate modification type
        validateModificationType(request.getModificationType(), order);

        // Calculate price adjustment if applicable
        Double priceAdjustment = calculatePriceAdjustment(request, order);

        OrderModification modification = OrderModification.builder()
                .order(order)
                .user(user)
                .modificationType(request.getModificationType())
                .previousValue(request.getPreviousValue())
                .newValue(request.getNewValue())
                .reason(request.getReason())
                .status("PENDING")
                .priceAdjustment(priceAdjustment)
                .build();

        modification = orderModificationRepository.save(modification);

        log.info("User {} requested modification {} for order {}", user.getEmail(), request.getModificationType(), order.getOrderNumber());

        // Notify admin
        notificationService.notifyOrderModificationRequest(modification.getId(), order.getOrderNumber(), request.getModificationType());

        return toResponse(modification);
    }

    public List<OrderModificationResponse> getMyModifications() {
        Long userId = getAuthenticatedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return orderModificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<OrderModificationResponse> getOrderModifications(Long orderId) {
        if (orderId == null) {
            throw new BadRequestException("Order ID is required");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return orderModificationRepository.findByOrderOrderByCreatedAtDesc(order).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // === Admin Methods ===

    public List<OrderModificationResponse> getAllPendingModifications() {
        return orderModificationRepository.findPendingModifications().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Long countPendingModifications() {
        return orderModificationRepository.countPendingModifications();
    }

    @Transactional
    public OrderModificationResponse approveModification(ApproveModificationRequest request) {
        if (request == null || request.getModificationId() == null) {
            throw new BadRequestException("Modification ID is required");
        }

        OrderModification modification = orderModificationRepository.findById(request.getModificationId())
                .orElseThrow(() -> new ResourceNotFoundException("OrderModification", "id", request.getModificationId()));

        if (!modification.canBeApproved()) {
            throw new BadRequestException("Modification cannot be approved");
        }

        Order order = modification.getOrder();

        // Apply the modification
        applyModification(modification, order);

        // Update modification status
        modification.approve(getAuthenticatedUserEmail());
        modification = orderModificationRepository.save(modification);

        // Update order
        orderRepository.save(order);

        log.info("Approved modification {} for order {}", modification.getId(), order.getOrderNumber());

        // Notify user
        notificationService.notifyOrderModificationApproved(modification.getId(), order.getOrderNumber(), modification.getModificationType());

        return toResponse(modification);
    }

    @Transactional
    public OrderModificationResponse rejectModification(RejectModificationRequest request) {
        if (request == null || request.getModificationId() == null) {
            throw new BadRequestException("Modification ID is required");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new BadRequestException("Rejection reason is required");
        }

        OrderModification modification = orderModificationRepository.findById(request.getModificationId())
                .orElseThrow(() -> new ResourceNotFoundException("OrderModification", "id", request.getModificationId()));

        if (!modification.canBeRejected()) {
            throw new BadRequestException("Modification cannot be rejected");
        }

        Order order = modification.getOrder();

        // Update modification status
        modification.reject(getAuthenticatedUserEmail(), request.getReason());
        modification = orderModificationRepository.save(modification);

        log.info("Rejected modification {} for order {}: {}", modification.getId(), order.getOrderNumber(), request.getReason());

        // Notify user
        notificationService.notifyOrderModificationRejected(modification.getId(), order.getOrderNumber(), request.getReason());

        return toResponse(modification);
    }

    // === Helper Methods ===

    private boolean canOrderBeModified(Order order) {
        return order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED;
    }

    private void validateModificationType(String modificationType, Order order) {
        switch (modificationType) {
            case "ADD_ITEM":
            case "REMOVE_ITEM":
            case "UPDATE_QUANTITY":
            case "UPDATE_ADDRESS":
            case "CANCEL_ITEM":
                // All valid types
                break;
            default:
                throw new BadRequestException("Invalid modification type: " + modificationType);
        }
    }

    private Double calculatePriceAdjustment(OrderModificationRequest request, Order order) {
        return switch (request.getModificationType()) {
            case "ADD_ITEM" -> calculateAddItemPrice(request, order);
            case "REMOVE_ITEM" -> calculateRemoveItemPrice(request, order);
            case "UPDATE_QUANTITY" -> calculateUpdateQuantityPrice(request, order);
            case "CANCEL_ITEM" -> calculateCancelItemPrice(request, order);
            case "UPDATE_ADDRESS" -> 0.0;
            default -> throw new BadRequestException("Unknown modification type: " + request.getModificationType());
        };
    }

    private Double calculateAddItemPrice(OrderModificationRequest request, Order order) {
        try {
            JsonNode json = objectMapper.readTree(request.getNewValue());
            Long menuItemId = json.get("menuItemId").asLong();
            int quantity = json.get("quantity").asInt(1);
            MenuItem item = menuItemRepository.findById(menuItemId).orElse(null);
            if (item == null) return 0.0;
            return item.getPrice().multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP).doubleValue();
        } catch (Exception e) {
            log.warn("Failed to calculate ADD_ITEM price: {}", e.getMessage());
            return 0.0;
        }
    }

    private Double calculateRemoveItemPrice(OrderModificationRequest request, Order order) {
        try {
            JsonNode json = objectMapper.readTree(request.getPreviousValue());
            Long orderItemId = json.get("orderItemId").asLong();
            OrderItem item = orderItemRepository.findById(orderItemId).orElse(null);
            if (item == null) return 0.0;
            return item.getSubtotal().negate().setScale(2, RoundingMode.HALF_UP).doubleValue();
        } catch (Exception e) {
            log.warn("Failed to calculate REMOVE_ITEM price: {}", e.getMessage());
            return 0.0;
        }
    }

    private Double calculateUpdateQuantityPrice(OrderModificationRequest request, Order order) {
        try {
            JsonNode prev = objectMapper.readTree(request.getPreviousValue());
            JsonNode next = objectMapper.readTree(request.getNewValue());
            Long orderItemId = prev.get("orderItemId").asLong();
            int oldQty = prev.get("oldQuantity").asInt();
            int newQty = next.get("newQuantity").asInt();
            OrderItem item = orderItemRepository.findById(orderItemId).orElse(null);
            if (item == null) return 0.0;
            BigDecimal unitPrice = item.getUnitPrice();
            return unitPrice.multiply(BigDecimal.valueOf(newQty - oldQty)).setScale(2, RoundingMode.HALF_UP).doubleValue();
        } catch (Exception e) {
            log.warn("Failed to calculate UPDATE_QUANTITY price: {}", e.getMessage());
            return 0.0;
        }
    }

    private Double calculateCancelItemPrice(OrderModificationRequest request, Order order) {
        return calculateRemoveItemPrice(request, order);
    }

    private void applyModification(OrderModification modification, Order order) {
        switch (modification.getModificationType()) {
            case "UPDATE_ADDRESS":
                if (modification.getNewValue() != null) {
                    order.setDeliveryAddress(modification.getNewValue());
                }
                break;
            case "ADD_ITEM":
                applyAddItem(modification, order);
                break;
            case "REMOVE_ITEM":
                applyRemoveItem(modification, order);
                break;
            case "UPDATE_QUANTITY":
                applyUpdateQuantity(modification, order);
                break;
            case "CANCEL_ITEM":
                applyCancelItem(modification, order);
                break;
            default:
                break;
        }

        recalculateOrderTotals(order);
        modification.complete();
    }

    private void applyAddItem(OrderModification modification, Order order) {
        try {
            JsonNode json = objectMapper.readTree(modification.getNewValue());
            Long menuItemId = json.get("menuItemId").asLong();
            int quantity = json.get("quantity").asInt(1);

            MenuItem menuItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new BadRequestException("Menu item not found: " + menuItemId));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .menuItemName(menuItem.getName())
                    .quantity(quantity)
                    .unitPrice(menuItem.getPrice())
                    .subtotal(menuItem.getPrice().multiply(BigDecimal.valueOf(quantity)))
                    .build();
            orderItemRepository.save(orderItem);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid modification data: " + e.getMessage());
        }
    }

    private void applyRemoveItem(OrderModification modification, Order order) {
        try {
            JsonNode json = objectMapper.readTree(modification.getPreviousValue());
            Long orderItemId = json.get("orderItemId").asLong();

            OrderItem orderItem = orderItemRepository.findById(orderItemId)
                    .orElseThrow(() -> new BadRequestException("Order item not found: " + orderItemId));
            orderItemRepository.delete(orderItem);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid modification data: " + e.getMessage());
        }
    }

    private void applyUpdateQuantity(OrderModification modification, Order order) {
        try {
            JsonNode json = objectMapper.readTree(modification.getNewValue());
            Long orderItemId = json.get("orderItemId").asLong();
            int newQuantity = json.get("newQuantity").asInt();

            OrderItem orderItem = orderItemRepository.findById(orderItemId)
                    .orElseThrow(() -> new BadRequestException("Order item not found: " + orderItemId));

            if (newQuantity <= 0) {
                orderItemRepository.delete(orderItem);
            } else {
                orderItem.setQuantity(newQuantity);
                orderItem.setSubtotal(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity)));
                orderItemRepository.save(orderItem);
            }
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid modification data: " + e.getMessage());
        }
    }

    private void applyCancelItem(OrderModification modification, Order order) {
        applyRemoveItem(modification, order);
    }

    private void recalculateOrderTotals(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        BigDecimal subtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal delivery = order.getDeliveryCharge() != null ? order.getDeliveryCharge() : BigDecimal.ZERO;
        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal tip = order.getTipAmount() != null ? order.getTipAmount() : BigDecimal.ZERO;

        order.setSubtotalAmount(subtotal);
        order.setTaxAmount(tax);
        order.setTotalAmount(subtotal.add(tax).add(delivery).add(tip).subtract(discount));
    }

    private Long getAuthenticatedUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getId();
            }
        } catch (Exception e) {
            log.warn("Could not get authenticated user ID", e);
        }
        throw new BadRequestException("Could not authenticate user");
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

    private OrderModificationResponse toResponse(OrderModification modification) {
        return OrderModificationResponse.builder()
                .id(modification.getId())
                .orderId(modification.getOrder().getId())
                .orderNumber(modification.getOrder().getOrderNumber())
                .userId(modification.getUser().getId())
                .userName(modification.getUser().getName())
                .modificationType(modification.getModificationType())
                .previousValue(modification.getPreviousValue())
                .newValue(modification.getNewValue())
                .reason(modification.getReason())
                .status(modification.getStatus())
                .priceAdjustment(modification.getPriceAdjustment())
                .approvedBy(modification.getApprovedBy())
                .approvedAt(modification.getApprovedAt())
                .rejectedBy(modification.getRejectedBy())
                .rejectedAt(modification.getRejectedAt())
                .rejectedReason(modification.getRejectedReason())
                .createdAt(modification.getCreatedAt())
                .updatedAt(modification.getUpdatedAt())
                .canBeApproved(modification.canBeApproved())
                .canBeRejected(modification.canBeRejected())
                .statusDisplay(getStatusDisplay(modification.getStatus()))
                .build();
    }

    private String getStatusDisplay(String status) {
        return switch (status) {
            case "PENDING" -> "Pending Approval";
            case "APPROVED" -> "Approved";
            case "REJECTED" -> "Rejected";
            case "COMPLETED" -> "Completed";
            default -> status;
        };
    }
}
