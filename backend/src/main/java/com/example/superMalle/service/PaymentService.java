package com.example.superMalle.service;

import com.example.superMalle.annotation.Idempotent;
import com.example.superMalle.dto.payment.*;
import com.example.superMalle.dto.menu.PagedResponse;
import com.example.superMalle.entity.Order;
import com.example.superMalle.entity.Payment;
import com.example.superMalle.entity.User;
import com.example.superMalle.entity.enums.OrderStatus;
import com.example.superMalle.entity.enums.PaymentStatus;
import com.example.superMalle.entity.enums.RefundStatus;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.exception.PaymentException;
import com.example.superMalle.exception.ResourceNotFoundException;
import com.example.superMalle.repository.OrderRepository;
import com.example.superMalle.repository.PaymentRepository;
import com.example.superMalle.repository.RefundRepository;
import com.example.superMalle.repository.UserRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Customer;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.CustomerCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ResilientPaymentService resilientPaymentService;

    @Transactional
    @Idempotent(key = "#request.header('Idempotency-Key')", ttlHours = 24, entity = "payment", strictKeyFormat = true)
    public PaymentIntentResponse createPaymentIntent(Long orderId, String paymentMethodType, String userEmail, @org.springframework.web.bind.annotation.RequestHeader("Idempotency-Key") String idempotencyKey) {
        if (orderId == null) {
            throw new BadRequestException("Order ID is required");
        }
        if (userEmail == null || userEmail.isBlank()) {
            throw new BadRequestException("User email is required");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getUser() == null) {
            throw new BadRequestException("Order has no associated user");
        }

        // Verify order belongs to user
        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new BadRequestException("Order does not belong to authenticated user");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot pay for a cancelled order");
        }

        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Order total must be greater than zero");
        }

        // Check if payment already exists for this order
        var existingPayment = paymentRepository.findByOrderId(orderId);
        if (existingPayment.isPresent() && existingPayment.get().getStatus() == PaymentStatus.SUCCEEDED) {
            throw new BadRequestException("Payment already completed for this order");
        }

        // Get or create Stripe customer
        String stripeCustomerId = getOrCreateStripeCustomer(order.getUser());

        try {
            PaymentIntent paymentIntent = resilientPaymentService.createPaymentIntentWithTimeout(
                    order.getTotalAmount(), "usd", stripeCustomerId, order.getId(), idempotencyKey);

        Payment payment = Payment.builder()
                .order(order)
                .stripePaymentIntentId(paymentIntent.getId())
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PROCESSING)
                .paymentMethodType(paymentMethodType)
                .metadata(paymentIntent.getClientSecret())
                .idempotencyKey(idempotencyKey)
                .build();
            payment = paymentRepository.save(payment);

            return PaymentIntentResponse.builder()
                    .clientSecret(paymentIntent.getClientSecret())
                    .paymentIntentId(paymentIntent.getId())
                    .amount(order.getTotalAmount())
                    .paymentId(payment.getId())
                    .build();
        } catch (Exception e) {
            log.error("Failed to create PaymentIntent for order {}: {}", orderId, e.getMessage(), e);
            throw new PaymentException("Payment initiation failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void handlePaymentSuccess(String stripePaymentIntentId) {
        if (stripePaymentIntentId == null || stripePaymentIntentId.isBlank()) {
            log.error("handlePaymentSuccess called with null/blank paymentIntentId");
            return;
        }

        Payment payment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "stripePaymentIntentId", stripePaymentIntentId));

        payment.setStatus(PaymentStatus.SUCCEEDED);
        paymentRepository.save(payment);

        // Update order status
        Order order = payment.getOrder();
        if (order == null) {
            log.error("Payment {} has no associated order", payment.getId());
            return;
        }
        order.setPaymentStatus(PaymentStatus.SUCCEEDED);
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
        }
        orderRepository.save(order);

        log.info("Payment succeeded for order {}", order.getId());

        // Notify admin and customer of payment success
        notificationService.notifyOrderStatusUpdate(order.getId(),
                order.getOrderNumber(), "PAYMENT_SUCCEEDED",
                PaymentStatus.PROCESSING.name());
    }

    @Transactional
    public void handlePaymentFailure(String stripePaymentIntentId) {
        if (stripePaymentIntentId == null || stripePaymentIntentId.isBlank()) {
            log.error("handlePaymentFailure called with null/blank paymentIntentId");
            return;
        }

        Payment payment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                .orElse(null);
        if (payment == null) return;

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        if (order == null) {
            log.error("Payment {} has no associated order", payment.getId());
            return;
        }
        order.setPaymentStatus(PaymentStatus.FAILED);
        orderRepository.save(order);

        log.info("Payment failed for order {}", order.getId());

        // Notify of payment failure
        notificationService.notifyOrderStatusUpdate(order.getId(),
                order.getOrderNumber(), "PAYMENT_FAILED",
                PaymentStatus.PROCESSING.name());
    }

    @Transactional
    public void handleDispute(String stripePaymentIntentId) {
        if (stripePaymentIntentId == null || stripePaymentIntentId.isBlank()) {
            log.error("handleDispute called with null/blank paymentIntentId");
            return;
        }

        Payment payment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                .orElse(null);
        if (payment == null) {
            log.warn("Dispute for unknown payment intent: {}", stripePaymentIntentId);
            return;
        }

        payment.setStatus(PaymentStatus.DISPUTED);
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        if (order != null) {
            order.setPaymentStatus(PaymentStatus.DISPUTED);
            orderRepository.save(order);
        }

        log.warn("Payment disputed for order {}: {}", order != null ? order.getId() : "unknown", stripePaymentIntentId);

        notificationService.notifyOrderStatusUpdate(
                order != null ? order.getId() : null,
                order != null ? order.getOrderNumber() : null,
                "PAYMENT_DISPUTED",
                PaymentStatus.SUCCEEDED.name());
    }

    @Transactional
    public void handlePaymentCancellation(String stripePaymentIntentId) {
        if (stripePaymentIntentId == null || stripePaymentIntentId.isBlank()) {
            log.error("handlePaymentCancellation called with null/blank paymentIntentId");
            return;
        }

        Payment payment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                .orElse(null);
        if (payment == null) return;

        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        if (order != null) {
            order.setPaymentStatus(PaymentStatus.CANCELLED);
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CANCELLED);
            }
            orderRepository.save(order);
        }

        log.info("Payment cancelled for order {}: {}", order != null ? order.getId() : "unknown", stripePaymentIntentId);

        notificationService.notifyOrderStatusUpdate(
                order != null ? order.getId() : null,
                order != null ? order.getOrderNumber() : null,
                "PAYMENT_CANCELLED",
                PaymentStatus.PROCESSING.name());
    }

    @Transactional
    public void handleRefundUpdate(String stripeRefundId, String refundStatus) {
        if (stripeRefundId == null || stripeRefundId.isBlank()) return;

        com.example.superMalle.entity.Refund refund = refundRepository.findByStripeRefundId(stripeRefundId)
                .orElse(null);
        if (refund == null) {
            log.warn("Refund update for unknown refund: {}", stripeRefundId);
            return;
        }

        RefundStatus newStatus;
        switch (refundStatus != null ? refundStatus.toLowerCase() : "") {
            case "succeeded" -> newStatus = RefundStatus.SUCCEEDED;
            case "failed" -> newStatus = RefundStatus.FAILED;
            default -> {
                log.info("Refund {} status update ignored: {}", stripeRefundId, refundStatus);
                return;
            }
        }
        refund.setStatus(newStatus);
        refundRepository.save(refund);

        log.info("Refund {} status updated to {}", stripeRefundId, newStatus);
    }

    @Transactional
    public RefundResponse processRefund(Long paymentId, BigDecimal amount, String reason) {
        if (paymentId == null) {
            throw new BadRequestException("Payment ID is required");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCEEDED && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new BadRequestException("Payment is not in a refundable state");
        }

        if (payment.getAmount() == null) {
            throw new BadRequestException("Payment amount is unknown");
        }

        long refundAmountCents;
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            if (amount.compareTo(payment.getAmount()) > 0) {
                throw new BadRequestException("Refund amount cannot exceed payment amount");
            }
            refundAmountCents = amount.multiply(BigDecimal.valueOf(100)).longValue();
        } else {
            refundAmountCents = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        }

        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getStripePaymentIntentId())
                    .setAmount(refundAmountCents)
                    .setReason(RefundCreateParams.Reason.valueOf(reason != null ? reason.toUpperCase().replace("-", "_") : "REQUESTED_BY_CUSTOMER"))
                    .build();

            com.stripe.model.Refund stripeRefund = com.stripe.model.Refund.create(params);

            boolean isPartial = amount != null && amount.compareTo(payment.getAmount()) < 0;
            payment.setStatus(isPartial ? PaymentStatus.PARTIALLY_REFUNDED : PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            // Update order
            Order order = payment.getOrder();
            if (order != null) {
                order.setPaymentStatus(payment.getStatus());
                if (!isPartial) {
                    order.setStatus(OrderStatus.CANCELLED);
                }
                orderRepository.save(order);
            }

            com.example.superMalle.entity.Refund refundEntity = com.example.superMalle.entity.Refund.builder()
                    .payment(payment)
                    .stripeRefundId(stripeRefund.getId())
                    .amount(BigDecimal.valueOf(stripeRefund.getAmount()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                    .reason(reason)
                    .status(RefundStatus.SUCCEEDED)
                    .build();
            refundEntity = refundRepository.save(refundEntity);

            return toRefundResponse(refundEntity);
        } catch (Exception e) {
            log.error("Refund failed for payment {}: {}", paymentId, e.getMessage(), e);
            throw new PaymentException("Refund failed: " + e.getMessage(), e);
        }
    }

    public PaymentResponse getPaymentByOrderId(Long orderId, String userEmail) {
        if (orderId == null) {
            throw new BadRequestException("Order ID is required");
        }
        if (userEmail == null || userEmail.isBlank()) {
            throw new BadRequestException("User email is required");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        if (order.getUser() == null || !order.getUser().getEmail().equals(userEmail)) {
            throw new BadRequestException("You do not have access to this payment");
        }
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return toPaymentResponse(payment);
    }

    /**
     * Admin-only: get payment by order ID without user ownership check.
     */
    public PaymentResponse getPaymentByOrderIdForAdmin(Long orderId) {
        if (orderId == null) {
            throw new BadRequestException("Order ID is required");
        }
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return toPaymentResponse(payment);
    }

    public PagedResponse<PaymentResponse> getPaymentHistory(int page, int size,
                                                             String status, LocalDate from, LocalDate to) {
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100;

        PaymentStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = PaymentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid payment status: " + status);
            }
        }

        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt = to != null ? to.atTime(LocalTime.MAX) : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Payment> paymentPage = paymentRepository.findFiltered(statusEnum, fromDt, toDt, pageable);
        return PagedResponse.<PaymentResponse>builder()
                .items(paymentPage.getContent().stream().map(this::toPaymentResponse).toList())
                .total(paymentPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(paymentPage.getTotalPages())
                .build();
    }

    private String getOrCreateStripeCustomer(User user) {
        if (user == null) {
            throw new BadRequestException("User is required for Stripe customer creation");
        }
        if (user.getStripeCustomerId() != null && !user.getStripeCustomerId().isBlank()) {
            return user.getStripeCustomerId();
        }

        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(user.getEmail())
                    .setName(user.getName() != null ? user.getName() : "")
                    .setPhone(user.getPhone() != null ? user.getPhone() : "")
                    .putMetadata("user_id", user.getId().toString())
                    .build();

            Customer customer = Customer.create(params);
            user.setStripeCustomerId(customer.getId());
            userRepository.save(user);
            return customer.getId();
        } catch (Exception e) {
            log.error("Failed to create Stripe customer for user {}: {}", user.getId(), e.getMessage(), e);
            throw new PaymentException("Failed to create payment customer", e);
        }
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        List<RefundResponse> refundResponses = refundRepository.findByPaymentId(payment.getId())
                .stream().map(this::toRefundResponse).toList();

        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                .orderNumber(payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null)
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .paymentMethodType(payment.getPaymentMethodType())
                .cardLast4(payment.getCardLast4())
                .cardBrand(payment.getCardBrand())
                .receiptUrl(payment.getReceiptUrl())
                .createdAt(payment.getCreatedAt())
                .refunds(refundResponses)
                .build();
    }

    private RefundResponse toRefundResponse(com.example.superMalle.entity.Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .stripeRefundId(refund.getStripeRefundId())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .status(refund.getStatus() != null ? refund.getStatus().name() : null)
                .createdAt(refund.getCreatedAt())
                .build();
    }
}
