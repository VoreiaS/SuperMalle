package com.example.superMalle.service;

import com.example.superMalle.dto.order.*;
import com.example.superMalle.dto.menu.PagedResponse;
import com.example.superMalle.entity.*;
import com.example.superMalle.entity.enums.DiscountType;
import com.example.superMalle.entity.enums.OrderStatus;
import com.example.superMalle.entity.enums.OrderType;
import com.example.superMalle.entity.enums.PaymentStatus;
import com.example.superMalle.entity.enums.TaxCategory;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.exception.InventoryConflictException;
import com.example.superMalle.exception.ResourceNotFoundException;
import com.example.superMalle.repository.*;
import com.example.superMalle.security.CustomUserDetails;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final CartRepository cartRepository;
    private final CouponRepository couponRepository;
    private final ReviewRepository reviewRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final OperatingHoursRepository operatingHoursRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final InventoryRepository inventoryRepository;
    private final Environment environment;

    @Value("${app.restaurant.tax-rate}")
    private double taxRate;

    private Map<String, Double> taxRates;

    @PostConstruct
    public void initTaxRates() {
        Map<String, Double> rates = new java.util.HashMap<>();
        rates.put("STANDARD", 0.08);
        rates.put("ALCOHOL", 0.10);
        rates.put("PREPARED_FOOD", 0.08);
        rates.put("GROCERY", 0.0);
        String raw = environment.getProperty("app.restaurant.tax-rates");
        if (raw != null && !raw.isBlank()) {
            try {
                String cleaned = raw.replace("{", "").replace("}", "");
                for (String entry : cleaned.split(",")) {
                    String[] parts = entry.trim().split(":");
                    if (parts.length == 2) {
                        rates.put(parts[0].trim(), Double.parseDouble(parts[1].trim()));
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse tax-rates config '{}', using defaults", raw);
            }
        }
        taxRates = java.util.Collections.unmodifiableMap(rates);
    }

    @Value("${app.restaurant.delivery-charge}")
    private BigDecimal deliveryCharge;

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        // Retry logic for optimistic locking failures (inventory concurrency)
        int maxRetries = 3;
        long baseDelayMs = 100;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return placeOrderInternal(request);
            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("Optimistic lock conflict on order placement (attempt {}/{}): {}", 
                        attempt, maxRetries, e.getMessage());
                
                if (attempt == maxRetries) {
                    throw new InventoryConflictException(
                            "Unable to place order due to concurrent inventory changes. Please retry.", e);
                }
                
                // Exponential backoff with jitter
                try {
                    long delay = baseDelayMs * (1L << (attempt - 1));
                    long jitter = (long) (Math.random() * delay * 0.1);
                    Thread.sleep(delay + jitter);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new InventoryConflictException("Order placement interrupted", ie);
                }
            }
        }
        
        // Should never reach here
        throw new InventoryConflictException("Max retries exceeded for order placement");
    }
    
    /**
     * Internal method containing the actual order placement logic.
     * Separated to enable retry on optimistic lock failures.
     */
    @Transactional
    public OrderResponse placeOrderInternal(PlaceOrderRequest request) {
        if (request == null) {
            throw new BadRequestException("Order request cannot be null");
        }
        if (request.getOrderType() == null || request.getOrderType().isBlank()) {
            throw new BadRequestException("Order type is required");
        }
        if (request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()) {
            throw new BadRequestException("Payment method is required");
        }

        Long userId = getAuthenticatedUserId();
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        OrderType orderType;
        try {
            orderType = OrderType.valueOf(request.getOrderType());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order type: " + request.getOrderType());
        }

        if (orderType == OrderType.DELIVERY && (request.getDeliveryAddress() == null || request.getDeliveryAddress().isBlank())) {
            throw new BadRequestException("Delivery address is required for delivery orders");
        }

        java.time.DayOfWeek today = java.time.LocalDate.now().getDayOfWeek();
        OperatingHours todaysHours = operatingHoursRepository.findByDayOfWeek(today).orElse(null);
        if (todaysHours == null || Boolean.TRUE.equals(todaysHours.getIsClosed())) {
            throw new BadRequestException("Restaurant is closed today. Please check our operating hours.");
        }
        java.time.LocalTime now = java.time.LocalTime.now();
        if (now.isBefore(todaysHours.getOpenTime()) || now.isAfter(todaysHours.getCloseTime())) {
            throw new BadRequestException("Restaurant is currently closed. Operating hours: " 
                + todaysHours.getOpenTime() + " - " + todaysHours.getCloseTime());
        }

        // Calculate amounts with per-item tax
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            if (cartItem.getMenuItem() == null || cartItem.getUnitPrice() == null) continue;
            BigDecimal itemSubtotal = cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);

            TaxCategory cat = cartItem.getMenuItem().getTaxCategory();
            if (cat == null) cat = TaxCategory.STANDARD;
            double itemTaxRate = taxRates.getOrDefault(cat.name(), taxRate);
            tax = tax.add(itemSubtotal.multiply(BigDecimal.valueOf(itemTaxRate)));
        }

        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Order total must be greater than zero");
        }

        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal deliveryFee = orderType == OrderType.DELIVERY ? deliveryCharge : BigDecimal.ZERO;

        // Apply coupon
        Coupon coupon = null;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            coupon = couponRepository.findByCodeAndIsActiveTrue(request.getCouponCode())
                    .orElseThrow(() -> new BadRequestException("Invalid or expired coupon code"));
            
            if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Coupon has expired");
            }
            if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
                throw new BadRequestException("Coupon usage limit reached");
            }
            if (coupon.getMinOrderAmount() != null && subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
                throw new BadRequestException("Order amount does not meet coupon minimum");
            }
            
            if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
                discount = subtotal.multiply(coupon.getValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                    discount = coupon.getMaxDiscountAmount();
                }
            } else {
                discount = coupon.getValue();
            }

            if (couponUsageRepository.existsByUserIdAndCouponId(userId, coupon.getId())) {
                throw new BadRequestException("You have already used this coupon");
            }

            // Ensure discount does not exceed subtotal
            if (discount.compareTo(subtotal) > 0) {
                discount = subtotal;
            }
            
            coupon.setUsageCount(coupon.getUsageCount() + 1);
            couponRepository.save(coupon);
        }

        BigDecimal tipAmount = request.getTipAmount() != null ? request.getTipAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(tax).add(deliveryFee).add(tipAmount).subtract(discount);

        // Safety check: total should not be negative
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        // Create order
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Order order = Order.builder()
                .user(user)
                .orderType(orderType)
                .status(OrderStatus.PENDING)
                .subtotalAmount(subtotal)
                .taxAmount(tax)
                .totalAmount(totalAmount)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryCharge(deliveryFee)
                .specialInstructions(request.getSpecialInstructions())
                .couponCode(request.getCouponCode())
                .discountAmount(discount)
                .tipAmount(tipAmount)
                .build();

        order = orderRepository.save(order);

        // Track coupon usage if coupon was applied
        if (coupon != null) {
            CouponUsage couponUsage = CouponUsage.builder()
                    .user(user)
                    .coupon(coupon)
                    .orderId(order.getId())
                    .build();
            couponUsageRepository.save(couponUsage);
        }

        // Validate stock availability for all items before creating order items
        for (CartItem cartItem : cart.getItems()) {
            if (cartItem.getMenuItem() == null) continue;
            Inventory inventory = inventoryRepository.findByMenuItemId(cartItem.getMenuItem().getId()).orElse(null);
            if (inventory != null && inventory.getQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for " + cartItem.getMenuItem().getName()
                    + ". Available: " + inventory.getQuantity() + ", requested: " + cartItem.getQuantity());
            }
        }

        // Create order items from cart
        for (CartItem cartItem : cart.getItems()) {
            if (cartItem.getMenuItem() == null) {
                log.warn("Skipping cart item with null menuItem for cart {}", cart.getId());
                continue;
            }
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(cartItem.getMenuItem())
                    .menuItemName(cartItem.getMenuItem().getName())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .customizations(cartItem.getCustomizations())
                    .subtotal(cartItem.getUnitPrice() != null ?
                            cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())) :
                            BigDecimal.ZERO)
                    .build();
            orderItemRepository.save(orderItem);
        }

        // Create initial status log
        OrderStatusLog statusLog = OrderStatusLog.builder()
                .order(order)
                .status(OrderStatus.PENDING)
                .changedBy(user.getEmail())
                .note("Order placed")
                .build();
        orderStatusLogRepository.save(statusLog);

        // Clear cart after order
        if (cart.getItems() != null) {
            cart.getItems().clear();
        }
        cartRepository.save(cart);

        try {
            emailService.sendOrderConfirmation(
                    order.getUser().getEmail(),
                    order.getOrderNumber(),
                    order.getTotalAmount().doubleValue(),
                    order.getDeliveryAddress(),
                    order.getEstimatedReadyAt() != null ? order.getEstimatedReadyAt().toString() : null
            );
        } catch (Exception e) {
            log.warn("Failed to send order confirmation email: {}", e.getMessage());
        }

        // Notify admin and kitchen via WebSocket
        List<Map<String, Object>> kitchenItems = orderItemRepository.findByOrderId(order.getId())
                .stream()
                .map(item -> Map.<String, Object>of(
                        "name", (Object) (item.getMenuItemName() != null ? item.getMenuItemName() : "Unknown"),
                        "quantity", (Object) item.getQuantity()
                ))
                .toList();
        notificationService.notifyNewOrder(order.getId(), order.getOrderNumber(),
                user.getName(), order.getTotalAmount().doubleValue());
        notificationService.notifyKitchenNewOrder(order.getId(), order.getOrderNumber(), kitchenItems);

        return getOrderResponse(order.getId());
    }

    public PagedResponse<OrderResponse> getMyOrders(int page, int size) {
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100;

        Long userId = getAuthenticatedUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PagedResponse.<OrderResponse>builder()
                .items(orderPage.getContent().stream().map(o -> toResponse(o, false)).toList())
                .total(orderPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(orderPage.getTotalPages())
                .build();
    }

    public OrderResponse getOrderById(Long id) {
        if (id == null) {
            throw new BadRequestException("Order ID is required");
        }
        Long userId = getAuthenticatedUserId();
        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return toResponse(order, true);
    }

    public OrderResponse getOrderStatus(Long id) {
        if (id == null) {
            throw new BadRequestException("Order ID is required");
        }
        Long userId = getAuthenticatedUserId();
        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .estimatedReadyAt(order.getEstimatedReadyAt())
                .build();
    }

    @Transactional
    public void cancelOrder(Long id) {
        cancelOrder(id, null);
    }

    @Transactional
    public void cancelOrder(Long id, String reason) {
        if (id == null) {
            throw new BadRequestException("Order ID is required");
        }
        Long userId = getAuthenticatedUserId();
        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Order cannot be cancelled at this stage");
        }
        
        String previousStatus = order.getStatus().name();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCompletedAt(null);
        if (reason != null) {
            order.setCancellationReason(reason);
        }
        orderRepository.save(order);

        OrderStatusLog statusLog = OrderStatusLog.builder()
                .order(order)
                .status(OrderStatus.CANCELLED)
                .changedBy(getAuthenticatedUserEmail())
                .note("Cancelled by customer" + (reason != null ? ": " + reason : ""))
                .build();
        orderStatusLogRepository.save(statusLog);

        notificationService.notifyOrderStatusUpdate(order.getId(), order.getOrderNumber(),
                OrderStatus.CANCELLED.name(), previousStatus);
    }

    @Transactional
    public ReviewResponse addReview(Long orderId, ReviewRequest request) {
        if (orderId == null) {
            throw new BadRequestException("Order ID is required");
        }
        if (request == null) {
            throw new BadRequestException("Review request cannot be null");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        Long userId = getAuthenticatedUserId();
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BadRequestException("Can only review completed orders");
        }

        // Check for duplicate review
        if (reviewRepository.findByOrderIdAndUserId(orderId, userId).isPresent()) {
            throw new BadRequestException("You have already reviewed this order");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Use proper MenuItem reference or null if not specified
        com.example.superMalle.entity.MenuItem menuItemEntity = null;
        if (request.getMenuItemId() != null) {
            menuItemEntity = menuItemRepository.findById(request.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", request.getMenuItemId()));
        }

        Review review = Review.builder()
                .user(user)
                .menuItem(menuItemEntity)
                .order(order)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        review = reviewRepository.save(review);

        return ReviewResponse.builder()
                .id(review.getId())
                .userId(userId)
                .userName(user.getName())
                .menuItemId(request.getMenuItemId())
                .orderId(orderId)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    // === Admin methods ===

    public PagedResponse<OrderResponse> getAllOrders(OrderStatus status, LocalDateTime from, LocalDateTime to, int page, int size) {
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findWithFilters(status, from, to, pageable);
        return PagedResponse.<OrderResponse>builder()
                .items(orderPage.getContent().stream().map(o -> toResponse(o, true)).toList())
                .total(orderPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(orderPage.getTotalPages())
                .build();
    }

    public OrderResponse getOrderDetails(Long id) {
        if (id == null) {
            throw new BadRequestException("Order ID is required");
        }
        return getOrderResponse(id);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        if (id == null) {
            throw new BadRequestException("Order ID is required");
        }
        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new BadRequestException("Status is required");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status: " + request.getStatus());
        }

        OrderStatus oldStatus = order.getStatus();
        
        // Validate status transition
        validateStatusTransition(oldStatus, newStatus);
        
        order.setStatus(newStatus);
        if (newStatus == OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        }
        order = orderRepository.save(order);

        OrderStatusLog statusLog = OrderStatusLog.builder()
                .order(order)
                .status(newStatus)
                .changedBy(getAuthenticatedUserEmail())
                .note(request.getNote())
                .build();
        orderStatusLogRepository.save(statusLog);

        try {
            emailService.sendOrderStatusUpdate(
                    order.getUser().getEmail(),
                    order.getOrderNumber(),
                    newStatus.name(),
                    request.getNote() != null ? request.getNote() : "Your order status has been updated"
            );
        } catch (Exception e) {
            log.warn("Failed to send status update email for order {}: {}", order.getId(), e.getMessage());
        }

        // Notify via WebSocket
        notificationService.notifyOrderStatusUpdate(order.getId(), order.getOrderNumber(),
                newStatus.name(), oldStatus.name());

        return toResponse(order, true);
    }

    @Transactional
    public OrderResponse adminCancelOrder(Long id, AdminCancelOrderRequest request) {
        if (id == null) {
            throw new BadRequestException("Order ID is required");
        }
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED
                || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel a " + order.getStatus().name().toLowerCase() + " order");
        }

        String previousStatus = order.getStatus().name();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCompletedAt(null);
        if (request != null && request.getReason() != null) {
            order.setCancellationReason(request.getReason());
        }
        order = orderRepository.save(order);

        OrderStatusLog statusLog = OrderStatusLog.builder()
                .order(order)
                .status(OrderStatus.CANCELLED)
                .changedBy(getAuthenticatedUserEmail())
                .note("Cancelled by admin" + (request != null && request.getReason() != null ? ": " + request.getReason() : ""))
                .build();
        orderStatusLogRepository.save(statusLog);

        notificationService.notifyOrderStatusUpdate(order.getId(), order.getOrderNumber(),
                OrderStatus.CANCELLED.name(), previousStatus);

        return toResponse(order, true);
    }

    @Transactional
    public OrderResponse updateEstimatedReadyAt(Long id, UpdateEtaRequest request) {
        if (id == null) {
            throw new BadRequestException("Order ID is required");
        }
        if (request == null || request.getEstimatedReadyAt() == null) {
            throw new BadRequestException("Estimated ready time is required");
        }
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        order.setEstimatedReadyAt(request.getEstimatedReadyAt());
        order = orderRepository.save(order);

        return toResponse(order, true);
    }

    private void validateStatusTransition(OrderStatus from, OrderStatus to) {
        if (from == null) {
            throw new BadRequestException("Current order status is unknown");
        }
        if (from == OrderStatus.COMPLETED || from == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot change status of a " + from.name().toLowerCase() + " order");
        }
        if (to == OrderStatus.PENDING) {
            throw new BadRequestException("Cannot revert order to pending");
        }
        if (from == to) {
            throw new BadRequestException("Order is already " + to.name().toLowerCase());
        }

        // Strict forward progression: only allow the next logical state
        boolean valid = switch (from) {
            case PENDING -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED -> to == OrderStatus.PREPARING || to == OrderStatus.CANCELLED;
            case PREPARING -> to == OrderStatus.READY || to == OrderStatus.CANCELLED;
            case READY -> to == OrderStatus.OUT_FOR_DELIVERY || to == OrderStatus.COMPLETED || to == OrderStatus.CANCELLED;
            case OUT_FOR_DELIVERY -> to == OrderStatus.DELIVERED || to == OrderStatus.CANCELLED;
            case DELIVERED -> to == OrderStatus.COMPLETED || to == OrderStatus.CANCELLED;
            default -> false;
        };

        if (!valid) {
            throw new BadRequestException("Cannot transition from " + from.name() + " to " + to.name());
        }
    }

    private OrderResponse getOrderResponse(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return toResponse(order, true);
    }

    private OrderResponse toResponse(Order order, boolean includeDetails) {
        OrderResponse.OrderResponseBuilder builder = OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderType(order.getOrderType() != null ? order.getOrderType().name() : null)
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .subtotalAmount(order.getSubtotalAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryCharge(order.getDeliveryCharge())
                .specialInstructions(order.getSpecialInstructions())
                .couponCode(order.getCouponCode())
                .discountAmount(order.getDiscountAmount())
                .tipAmount(order.getTipAmount())
                .cancellationReason(order.getCancellationReason())
                .estimatedReadyAt(order.getEstimatedReadyAt())
                .completedAt(order.getCompletedAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt());

        if (includeDetails) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            builder.items(items.stream().map(this::toOrderItemResponse).toList());

            List<OrderStatusLog> logs = orderStatusLogRepository.findByOrderIdOrderByCreatedAtDesc(order.getId());
            builder.statusLog(logs.stream().map(this::toStatusLogResponse).toList());
        }

        return builder.build();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItem() != null ? item.getMenuItem().getId() : null)
                .menuItemName(item.getMenuItemName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .customizations(item.getCustomizations())
                .subtotal(item.getSubtotal())
                .build();
    }

    private OrderStatusLogResponse toStatusLogResponse(OrderStatusLog logEntry) {
        return OrderStatusLogResponse.builder()
                .id(logEntry.getId())
                .status(logEntry.getStatus() != null ? logEntry.getStatus().name() : null)
                .changedBy(logEntry.getChangedBy())
                .note(logEntry.getNote())
                .createdAt(logEntry.getCreatedAt())
                .build();
    }

    private Long getAuthenticatedUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BadRequestException("No authenticated user found");
        }
        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BadRequestException("Invalid authentication principal");
        }
        return userDetails.getId();
    }

    private String getAuthenticatedUserEmail() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return "system";
        }
        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return "system";
        }
        return userDetails.getUsername();
    }
}
