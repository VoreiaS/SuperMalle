package com.example.superMalle.service;

import com.example.superMalle.dto.menu.PagedResponse;
import com.example.superMalle.dto.order.*;
import com.example.superMalle.entity.*;
import com.example.superMalle.entity.enums.*;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.repository.*;
import com.example.superMalle.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderStatusLogRepository orderStatusLogRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CouponRepository couponRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private EmailService emailService;
    @Mock
    private OperatingHoursRepository operatingHoursRepository;
    @Mock
    private CouponUsageRepository couponUsageRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private Environment environment;

    private OrderService orderService;

    private User testUser;
    private MenuItem testMenuItem;
    private Cart testCart;
    private CartItem testCartItem;
    private final Long testUserId = 1L;
    private final Long testOrderId = 100L;
    private final String testEmail = "customer@example.com";

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, orderItemRepository,
                orderStatusLogRepository, cartRepository, couponRepository,
                reviewRepository, menuItemRepository, userRepository,
                notificationService, emailService, operatingHoursRepository,
                couponUsageRepository, inventoryRepository, environment);

        ReflectionTestUtils.setField(orderService, "taxRate", 0.08);
        ReflectionTestUtils.setField(orderService, "deliveryCharge", BigDecimal.valueOf(5.00));
        orderService.initTaxRates();

        testUser = User.builder()
                .id(testUserId)
                .name("Customer")
                .email(testEmail)
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .build();

        testMenuItem = MenuItem.builder()
                .id(10L)
                .name("Test Burger")
                .price(BigDecimal.valueOf(12.99))
                .taxCategory(TaxCategory.STANDARD)
                .isAvailable(true)
                .build();

        testCartItem = CartItem.builder()
                .id(1L)
                .menuItem(testMenuItem)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(12.99))
                .build();

        testCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .items(new java.util.ArrayList<>(List.of(testCartItem)))
                .build();

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(new CustomUserDetails(testUser));
        SecurityContextHolder.setContext(securityContext);
    }

    private static OperatingHours allDayHours() {
        return OperatingHours.builder()
                .dayOfWeek(LocalDate.now().getDayOfWeek())
                .openTime(LocalTime.MIDNIGHT)
                .closeTime(LocalTime.of(23, 59))
                .isClosed(false)
                .build();
    }

    private Order orderWithId() {
        return Order.builder()
                .id(testOrderId)
                .orderNumber("ORD-TEST-123")
                .user(testUser)
                .orderType(OrderType.PICKUP)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .subtotalAmount(BigDecimal.valueOf(25.98))
                .taxAmount(BigDecimal.valueOf(2.08))
                .totalAmount(BigDecimal.valueOf(28.06))
                .paymentMethod("card")
                .build();
    }

    // === placeOrder ===

    @Test
    @DisplayName("Should place order successfully for pickup")
    void shouldPlaceOrderForPickup() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setOrderType("PICKUP");
        request.setPaymentMethod("card");

        when(cartRepository.findByUserId(testUserId)).thenReturn(Optional.of(testCart));
        when(operatingHoursRepository.findByDayOfWeek(any())).thenReturn(Optional.of(allDayHours()));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            ReflectionTestUtils.setField(o, "id", testOrderId);
            o.setOrderNumber("ORD-TEST-123");
            return o;
        });
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(i -> i.getArgument(0));
        when(orderStatusLogRepository.save(any(OrderStatusLog.class))).thenAnswer(i -> i.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(orderWithId()));

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response).isNotNull();
        verify(notificationService).notifyNewOrder(anyLong(), anyString(), anyString(), anyDouble());
        verify(notificationService).notifyKitchenNewOrder(anyLong(), anyString(), anyList());
    }

    @Test
    @DisplayName("Should place delivery order with delivery address")
    void shouldPlaceDeliveryOrder() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setOrderType("DELIVERY");
        request.setPaymentMethod("card");
        request.setDeliveryAddress("123 Main St");

        when(cartRepository.findByUserId(testUserId)).thenReturn(Optional.of(testCart));
        when(operatingHoursRepository.findByDayOfWeek(any())).thenReturn(Optional.of(allDayHours()));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            ReflectionTestUtils.setField(o, "id", testOrderId);
            o.setOrderNumber("ORD-TEST-123");
            return o;
        });
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(i -> i.getArgument(0));
        when(orderStatusLogRepository.save(any(OrderStatusLog.class))).thenAnswer(i -> i.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(orderWithId()));

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Should throw when cart is empty")
    void shouldThrowOnEmptyCart() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setOrderType("PICKUP");
        request.setPaymentMethod("card");

        Cart emptyCart = Cart.builder().id(1L).user(testUser).items(List.of()).build();
        when(cartRepository.findByUserId(testUserId)).thenReturn(Optional.of(emptyCart));

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    @DisplayName("Should throw when restaurant is closed")
    void shouldThrowWhenClosed() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setOrderType("PICKUP");
        request.setPaymentMethod("card");

        when(cartRepository.findByUserId(testUserId)).thenReturn(Optional.of(testCart));
        when(operatingHoursRepository.findByDayOfWeek(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Restaurant is closed");
    }

    @Test
    @DisplayName("Should throw when stock insufficient")
    void shouldThrowOnInsufficientStock() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setOrderType("PICKUP");
        request.setPaymentMethod("card");

        when(cartRepository.findByUserId(testUserId)).thenReturn(Optional.of(testCart));
        when(operatingHoursRepository.findByDayOfWeek(any())).thenReturn(Optional.of(allDayHours()));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        Inventory inventory = Inventory.builder().menuItem(testMenuItem).quantity(1).build();
        when(inventoryRepository.findByMenuItemId(10L)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("Should apply valid coupon to order")
    void shouldApplyCoupon() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setOrderType("PICKUP");
        request.setPaymentMethod("card");
        request.setCouponCode("SAVE10");

        Coupon coupon = Coupon.builder()
                .id(1L)
                .code("SAVE10")
                .discountType(DiscountType.PERCENTAGE)
                .value(BigDecimal.TEN)
                .isActive(true)
                .usageCount(0)
                .usageLimit(100)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .minOrderAmount(BigDecimal.valueOf(5))
                .build();

        when(cartRepository.findByUserId(testUserId)).thenReturn(Optional.of(testCart));
        when(operatingHoursRepository.findByDayOfWeek(any())).thenReturn(Optional.of(allDayHours()));
        when(couponRepository.findByCodeAndIsActiveTrue("SAVE10")).thenReturn(Optional.of(coupon));
        when(couponUsageRepository.existsByUserIdAndCouponId(testUserId, 1L)).thenReturn(false);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            ReflectionTestUtils.setField(o, "id", testOrderId);
            o.setOrderNumber("ORD-TEST-123");
            return o;
        });
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(i -> i.getArgument(0));
        when(orderStatusLogRepository.save(any(OrderStatusLog.class))).thenAnswer(i -> i.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(orderWithId()));
        when(couponUsageRepository.save(any(CouponUsage.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response).isNotNull();
        verify(couponUsageRepository).save(any(CouponUsage.class));
    }

    @Test
    @DisplayName("Should throw when coupon already used by user")
    void shouldThrowOnCouponAlreadyUsed() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setOrderType("PICKUP");
        request.setPaymentMethod("card");
        request.setCouponCode("SAVE10");

        Coupon coupon = Coupon.builder()
                .id(1L)
                .code("SAVE10")
                .discountType(DiscountType.PERCENTAGE)
                .value(BigDecimal.TEN)
                .isActive(true)
                .usageCount(0)
                .usageLimit(100)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .minOrderAmount(BigDecimal.valueOf(5))
                .build();

        when(cartRepository.findByUserId(testUserId)).thenReturn(Optional.of(testCart));
        when(operatingHoursRepository.findByDayOfWeek(any())).thenReturn(Optional.of(allDayHours()));
        when(couponRepository.findByCodeAndIsActiveTrue("SAVE10")).thenReturn(Optional.of(coupon));
        when(couponUsageRepository.existsByUserIdAndCouponId(testUserId, 1L)).thenReturn(true);

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already used this coupon");
    }

    // === getMyOrders ===

    @Test
    @DisplayName("Should return paginated orders for user")
    void shouldGetMyOrders() {
        org.springframework.data.domain.Page<Order> page = mock(org.springframework.data.domain.Page.class);
        when(page.getContent()).thenReturn(List.of());
        when(page.getTotalElements()).thenReturn(0L);
        when(page.getTotalPages()).thenReturn(0);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(eq(testUserId), any())).thenReturn(page);

        PagedResponse<OrderResponse> result = orderService.getMyOrders(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();
    }

    // === cancelOrder ===

    @Test
    @DisplayName("Should cancel pending order")
    void shouldCancelPendingOrder() {
        Order order = Order.builder()
                .id(testOrderId)
                .user(testUser)
                .orderNumber("ORD-100")
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findByIdAndUserId(testOrderId, testUserId)).thenReturn(Optional.of(order));

        orderService.cancelOrder(testOrderId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderStatusLogRepository).save(any(OrderStatusLog.class));
        verify(notificationService).notifyOrderStatusUpdate(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw when cancelling completed order")
    void shouldThrowOnCancelCompleted() {
        Order order = Order.builder()
                .id(testOrderId)
                .user(testUser)
                .status(OrderStatus.COMPLETED)
                .build();

        when(orderRepository.findByIdAndUserId(testOrderId, testUserId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(testOrderId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be cancelled");
    }

    // === addReview ===

    @Test
    @DisplayName("Should add review to completed order")
    void shouldAddReview() {
        Order order = Order.builder()
                .id(testOrderId)
                .user(testUser)
                .status(OrderStatus.COMPLETED)
                .build();

        when(orderRepository.findByIdAndUserId(testOrderId, testUserId)).thenReturn(Optional.of(order));
        when(reviewRepository.findByOrderIdAndUserId(testOrderId, testUserId)).thenReturn(Optional.empty());
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> {
            Review r = i.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 999L);
            return r;
        });

        ReviewRequest request = new ReviewRequest();
        request.setRating(5);
        request.setComment("Great food!");

        ReviewResponse response = orderService.addReview(testOrderId, request);

        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("Great food!");
    }

    @Test
    @DisplayName("Should throw when reviewing non-completed order")
    void shouldThrowReviewOnNonCompleted() {
        Order order = Order.builder()
                .id(testOrderId)
                .user(testUser)
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findByIdAndUserId(testOrderId, testUserId)).thenReturn(Optional.of(order));

        ReviewRequest request = new ReviewRequest();
        request.setRating(5);

        assertThatThrownBy(() -> orderService.addReview(testOrderId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Can only review completed orders");
    }

    @Test
    @DisplayName("Should throw on duplicate review")
    void shouldThrowOnDuplicateReview() {
        Order order = Order.builder()
                .id(testOrderId)
                .user(testUser)
                .status(OrderStatus.COMPLETED)
                .build();

        when(orderRepository.findByIdAndUserId(testOrderId, testUserId)).thenReturn(Optional.of(order));
        when(reviewRepository.findByOrderIdAndUserId(testOrderId, testUserId))
                .thenReturn(Optional.of(Review.builder().id(1L).build()));

        ReviewRequest request = new ReviewRequest();
        request.setRating(4);

        assertThatThrownBy(() -> orderService.addReview(testOrderId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already reviewed this order");
    }

    // === Admin: updateOrderStatus ===

    @Test
    @DisplayName("Should update order status as admin")
    void shouldUpdateOrderStatus() {
        Order order = Order.builder()
                .id(testOrderId)
                .orderNumber("ORD-100")
                .status(OrderStatus.PENDING)
                .orderType(OrderType.PICKUP)
                .user(testUser)
                .build();

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderStatusLogRepository.save(any(OrderStatusLog.class))).thenAnswer(i -> i.getArgument(0));
        when(orderItemRepository.findByOrderId(testOrderId)).thenReturn(List.of());
        when(orderStatusLogRepository.findByOrderIdOrderByCreatedAtDesc(testOrderId)).thenReturn(List.of());

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus("CONFIRMED");
        request.setNote("Confirmed by kitchen");

        OrderResponse response = orderService.updateOrderStatus(testOrderId, request);

        assertThat(response).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(notificationService).notifyOrderStatusUpdate(anyLong(), anyString(), eq("CONFIRMED"), eq("PENDING"));
    }

    @Test
    @DisplayName("Should throw on invalid status transition")
    void shouldThrowOnInvalidStatusTransition() {
        Order order = Order.builder()
                .id(testOrderId)
                .status(OrderStatus.CANCELLED)
                .build();

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus("CONFIRMED");

        assertThatThrownBy(() -> orderService.updateOrderStatus(testOrderId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot change status");
    }

    // === Admin: adminCancelOrder ===

    @Test
    @DisplayName("Should cancel order as admin")
    void shouldAdminCancelOrder() {
        Order order = Order.builder()
                .id(testOrderId)
                .user(testUser)
                .orderNumber("ORD-100")
                .status(OrderStatus.PENDING)
                .orderType(OrderType.PICKUP)
                .build();

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderStatusLogRepository.save(any(OrderStatusLog.class))).thenAnswer(i -> i.getArgument(0));
        when(orderItemRepository.findByOrderId(testOrderId)).thenReturn(List.of());
        when(orderStatusLogRepository.findByOrderIdOrderByCreatedAtDesc(testOrderId)).thenReturn(List.of());

        AdminCancelOrderRequest request = new AdminCancelOrderRequest();
        request.setReason("Out of stock");

        OrderResponse response = orderService.adminCancelOrder(testOrderId, request);

        assertThat(response).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancellationReason()).isEqualTo("Out of stock");
    }

    // === Admin: updateEstimatedReadyAt ===

    @Test
    @DisplayName("Should update estimated ready time")
    void shouldUpdateEta() {
        Order order = Order.builder()
                .id(testOrderId)
                .user(testUser)
                .orderNumber("ORD-100")
                .status(OrderStatus.CONFIRMED)
                .orderType(OrderType.PICKUP)
                .build();

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderItemRepository.findByOrderId(testOrderId)).thenReturn(List.of());
        when(orderStatusLogRepository.findByOrderIdOrderByCreatedAtDesc(testOrderId)).thenReturn(List.of());

        UpdateEtaRequest request = new UpdateEtaRequest();
        request.setEstimatedReadyAt(LocalDateTime.now().plusMinutes(20));

        OrderResponse response = orderService.updateEstimatedReadyAt(testOrderId, request);

        assertThat(response).isNotNull();
        assertThat(order.getEstimatedReadyAt()).isNotNull();
    }
}
