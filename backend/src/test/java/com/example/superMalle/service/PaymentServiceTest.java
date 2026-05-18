package com.example.superMalle.service;

import com.example.superMalle.dto.menu.PagedResponse;
import com.example.superMalle.dto.payment.PaymentIntentResponse;
import com.example.superMalle.dto.payment.PaymentResponse;
import com.example.superMalle.dto.payment.RefundResponse;
import com.example.superMalle.entity.*;
import com.example.superMalle.entity.enums.*;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.exception.PaymentException;
import com.example.superMalle.exception.ResourceNotFoundException;
import com.example.superMalle.repository.OrderRepository;
import com.example.superMalle.repository.PaymentRepository;
import com.example.superMalle.repository.RefundRepository;
import com.example.superMalle.repository.UserRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.param.RefundCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RefundRepository refundRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ResilientPaymentService resilientPaymentService;

    private PaymentService paymentService;

    private User testUser;
    private Order testOrder;
    private Payment testPayment;
    private final Long testOrderId = 100L;
    private final Long testPaymentId = 200L;
    private final String testEmail = "customer@example.com";
    private final String testPiId = "pi_test_123";

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, refundRepository,
                orderRepository, userRepository, notificationService,
                resilientPaymentService);

        testUser = User.builder()
                .id(1L)
                .name("Customer")
                .email(testEmail)
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .stripeCustomerId("cus_test_123")
                .build();

        testOrder = Order.builder()
                .id(testOrderId)
                .user(testUser)
                .orderNumber("ORD-123")
                .orderType(OrderType.PICKUP)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(50.00))
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        testPayment = Payment.builder()
                .id(testPaymentId)
                .order(testOrder)
                .stripePaymentIntentId(testPiId)
                .amount(BigDecimal.valueOf(50.00))
                .status(PaymentStatus.PROCESSING)
                .paymentMethodType("card")
                .build();
    }

    // === createPaymentIntent ===

    @Test
    @DisplayName("Should create payment intent successfully")
    void shouldCreatePaymentIntent() throws Exception {
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        PaymentIntent stripePi = mock(PaymentIntent.class);
        when(stripePi.getId()).thenReturn("pi_new_456");
        when(stripePi.getClientSecret()).thenReturn("secret_test");

        when(resilientPaymentService.createPaymentIntentWithTimeout(
                eq(BigDecimal.valueOf(50.00)), eq("usd"), eq("cus_test_123"),
                eq(testOrderId), anyString()))
                .thenReturn(stripePi);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentIntentResponse response = paymentService.createPaymentIntent(
                testOrderId, "card", testEmail, "idemp-123");

        assertThat(response).isNotNull();
        assertThat(response.getClientSecret()).isEqualTo("secret_test");
        assertThat(response.getPaymentIntentId()).isEqualTo("pi_new_456");
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
    }

    @Test
    @DisplayName("Should throw for cancelled order")
    void shouldThrowForCancelledOrder() {
        testOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> paymentService.createPaymentIntent(
                testOrderId, "card", testEmail, "key"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot pay for a cancelled order");
    }

    @Test
    @DisplayName("Should throw for existing completed payment")
    void shouldThrowForExistingPayment() {
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        Payment existing = Payment.builder()
                .id(999L)
                .order(testOrder)
                .status(PaymentStatus.SUCCEEDED)
                .build();
        when(paymentRepository.findByOrderId(testOrderId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> paymentService.createPaymentIntent(
                testOrderId, "card", testEmail, "key"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Payment already completed");
    }

    @Test
    @DisplayName("Should throw when order has no user")
    void shouldThrowForNoUser() {
        testOrder.setUser(null);
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> paymentService.createPaymentIntent(
                testOrderId, "card", testEmail, "key"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no associated user");
    }

    @Test
    @DisplayName("Should throw when order belongs to different user")
    void shouldThrowForWrongUser() {
        User otherUser = User.builder().id(2L).email("other@example.com").build();
        testOrder.setUser(otherUser);
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> paymentService.createPaymentIntent(
                testOrderId, "card", testEmail, "key"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not belong to authenticated user");
    }

    @Test
    @DisplayName("Should throw for zero total")
    void shouldThrowForZeroTotal() {
        testOrder.setTotalAmount(BigDecimal.ZERO);
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> paymentService.createPaymentIntent(
                testOrderId, "card", testEmail, "key"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must be greater than zero");
    }

    @Test
    @DisplayName("Should wrap Stripe exception into PaymentException")
    void shouldWrapStripeException() {
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(resilientPaymentService.createPaymentIntentWithTimeout(
                any(), anyString(), anyString(), anyLong(), anyString()))
                .thenThrow(new RuntimeException("Stripe API error"));

        assertThatThrownBy(() -> paymentService.createPaymentIntent(
                testOrderId, "card", testEmail, "key"))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("Payment initiation failed");
    }

    // === handlePaymentSuccess ===

    @Test
    @DisplayName("Should process successful payment")
    void shouldHandlePaymentSuccess() {
        when(paymentRepository.findByStripePaymentIntentId(testPiId))
                .thenReturn(Optional.of(testPayment));

        paymentService.handlePaymentSuccess(testPiId);

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(testOrder.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(notificationService).notifyOrderStatusUpdate(anyLong(), anyString(),
                eq("PAYMENT_SUCCEEDED"), anyString());
    }

    @Test
    @DisplayName("Should handle success for unknown payment intent silently")
    void shouldHandleUnknownPaymentSuccess() {
        when(paymentRepository.findByStripePaymentIntentId("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.handlePaymentSuccess("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should ignore null payment intent ID on success")
    void shouldIgnoreNullPaymentIntentOnSuccess() {
        paymentService.handlePaymentSuccess(null);
        paymentService.handlePaymentSuccess("");
        verifyNoInteractions(paymentRepository);
    }

    // === handlePaymentFailure ===

    @Test
    @DisplayName("Should process failed payment")
    void shouldHandlePaymentFailure() {
        when(paymentRepository.findByStripePaymentIntentId(testPiId))
                .thenReturn(Optional.of(testPayment));

        paymentService.handlePaymentFailure(testPiId);

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(testOrder.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(notificationService).notifyOrderStatusUpdate(anyLong(), anyString(),
                eq("PAYMENT_FAILED"), anyString());
    }

    @Test
    @DisplayName("Should ignore unknown payment intent on failure")
    void shouldHandleUnknownPaymentFailure() {
        when(paymentRepository.findByStripePaymentIntentId("unknown"))
                .thenReturn(Optional.empty());

        paymentService.handlePaymentFailure("unknown");
    }

    // === handleDispute ===

    @Test
    @DisplayName("Should mark payment as disputed")
    void shouldHandleDispute() {
        when(paymentRepository.findByStripePaymentIntentId(testPiId))
                .thenReturn(Optional.of(testPayment));

        paymentService.handleDispute(testPiId);

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.DISPUTED);
        assertThat(testOrder.getPaymentStatus()).isEqualTo(PaymentStatus.DISPUTED);
        verify(notificationService).notifyOrderStatusUpdate(anyLong(), anyString(),
                eq("PAYMENT_DISPUTED"), anyString());
    }

    // === handlePaymentCancellation ===

    @Test
    @DisplayName("Should mark payment as cancelled")
    void shouldHandlePaymentCancellation() {
        when(paymentRepository.findByStripePaymentIntentId(testPiId))
                .thenReturn(Optional.of(testPayment));

        paymentService.handlePaymentCancellation(testPiId);

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(testOrder.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(notificationService).notifyOrderStatusUpdate(anyLong(), anyString(),
                eq("PAYMENT_CANCELLED"), anyString());
    }

    // === processRefund ===

    @Test
    @DisplayName("Should process full refund")
    void shouldProcessFullRefund() throws Exception {
        testPayment.setStatus(PaymentStatus.SUCCEEDED);
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));
        when(refundRepository.save(any(com.example.superMalle.entity.Refund.class)))
                .thenAnswer(i -> i.getArgument(0));

        com.stripe.model.Refund stripeRefund = mock(com.stripe.model.Refund.class);
        when(stripeRefund.getId()).thenReturn("re_test_refund");
        when(stripeRefund.getAmount()).thenReturn(5000L);
        try (var mockedStatic = mockStatic(com.stripe.model.Refund.class)) {
            mockedStatic.when(() -> com.stripe.model.Refund.create(any(RefundCreateParams.class)))
                    .thenReturn(stripeRefund);

            RefundResponse response = paymentService.processRefund(testPaymentId, null, "requested_by_customer");

            assertThat(response).isNotNull();
            assertThat(response.getStripeRefundId()).isEqualTo("re_test_refund");
            assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }
    }

    @Test
    @DisplayName("Should throw when payment not refundable")
    void shouldThrowOnNonRefundablePayment() {
        testPayment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));

        assertThatThrownBy(() -> paymentService.processRefund(testPaymentId, null, "reason"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not in a refundable state");
    }

    @Test
    @DisplayName("Should throw when refund amount exceeds payment")
    void shouldThrowOnExcessiveRefund() {
        testPayment.setStatus(PaymentStatus.SUCCEEDED);
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));

        assertThatThrownBy(() -> paymentService.processRefund(
                testPaymentId, BigDecimal.valueOf(999.00), "reason"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot exceed payment amount");
    }

    // === getPaymentByOrderId ===

    @Test
    @DisplayName("Should get payment by order for owner")
    void shouldGetPaymentByOrder() {
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(testOrderId)).thenReturn(Optional.of(testPayment));
        when(refundRepository.findByPaymentId(testPaymentId)).thenReturn(List.of());

        PaymentResponse response = paymentService.getPaymentByOrderId(testOrderId, testEmail);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(testOrderId);
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
    }

    @Test
    @DisplayName("Should throw when non-owner gets payment")
    void shouldThrowOnWrongUserGetPayment() {
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> paymentService.getPaymentByOrderId(testOrderId, "other@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("do not have access");
    }

    // === getPaymentHistory ===

    @Test
    @DisplayName("Should return paginated payment history")
    void shouldGetPaymentHistory() {
        Page<Payment> page = mock(Page.class);
        when(page.getContent()).thenReturn(List.of(testPayment));
        when(page.getTotalElements()).thenReturn(1L);
        when(page.getTotalPages()).thenReturn(1);
        when(paymentRepository.findFiltered(any(), any(), any(), any(Pageable.class))).thenReturn(page);
        when(refundRepository.findByPaymentId(testPaymentId)).thenReturn(List.of());

        PagedResponse<PaymentResponse> result = paymentService.getPaymentHistory(0, 10, null, null, null);

        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getItems()).hasSize(1);
    }
}
