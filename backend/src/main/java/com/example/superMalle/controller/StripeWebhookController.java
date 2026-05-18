package com.example.superMalle.controller;

import com.example.superMalle.entity.IdempotencyKey;
import com.example.superMalle.entity.enums.IdempotencyStatus;
import com.example.superMalle.repository.IdempotencyKeyRepository;
import com.example.superMalle.service.PaymentService;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@RestController
@RequestMapping("/api/v1/payments/webhook")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final PaymentService paymentService;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    @Value("${app.stripe.webhook-secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("Received Stripe webhook event");

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (Exception e) {
            log.error("Stripe webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(400).body("Webhook signature verification failed");
        }

        String eventId = event.getId();
        if (eventId != null && !eventId.isBlank()) {
            var existing = idempotencyKeyRepository
                    .findByKeyAndEntityAndUserId(eventId, "stripe_webhook", null)
                    .orElse(null);
            if (existing != null && existing.isCompleted()) {
                log.info("Stripe webhook event {} already processed, returning cached response", eventId);
                return ResponseEntity.ok("Webhook already processed");
            }
        }

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject;
        try {
            stripeObject = deserializer.getObject().orElseThrow(
                () -> new IllegalStateException("Failed to deserialize Stripe event data")
            );
        } catch (Exception e) {
            log.error("Failed to deserialize Stripe event {}: {}", event.getType(), e.getMessage());
            return ResponseEntity.status(500).body("Failed to deserialize event data");
        }

        try {
            switch (event.getType()) {
                case "payment_intent.succeeded" -> {
                    PaymentIntent pi = (PaymentIntent) stripeObject;
                    log.info("PaymentIntent succeeded: {}", pi.getId());
                    paymentService.handlePaymentSuccess(pi.getId());
                }
                case "payment_intent.payment_failed" -> {
                    PaymentIntent pi = (PaymentIntent) stripeObject;
                    log.info("PaymentIntent failed: {}", pi.getId());
                    paymentService.handlePaymentFailure(pi.getId());
                }
                case "payment_intent.canceled" -> {
                    PaymentIntent pi = (PaymentIntent) stripeObject;
                    log.info("PaymentIntent canceled: {}", pi.getId());
                    paymentService.handlePaymentCancellation(pi.getId());
                }
                case "charge.refunded" -> {
                    Charge charge = (Charge) stripeObject;
                    log.info("Charge refunded: {}", charge.getId());
                    var refunds = charge.getRefunds();
                    if (refunds != null && refunds.getData() != null && !refunds.getData().isEmpty()) {
                        com.stripe.model.Refund latestRefund = refunds.getData().get(0);
                        paymentService.handleRefundUpdate(latestRefund.getId(), latestRefund.getStatus());
                    }
                }
                case "charge.refund.updated" -> {
                    Charge charge = (Charge) stripeObject;
                    var refunds = charge.getRefunds();
                    if (refunds != null && refunds.getData() != null && !refunds.getData().isEmpty()) {
                        com.stripe.model.Refund updatedRefund = refunds.getData().get(0);
                        log.info("Refund updated: {} status={}", updatedRefund.getId(), updatedRefund.getStatus());
                        paymentService.handleRefundUpdate(updatedRefund.getId(), updatedRefund.getStatus());
                    }
                }
                case "charge.dispute.created" -> {
                    Charge charge = (Charge) stripeObject;
                    String paymentIntentId = charge.getPaymentIntent();
                    log.warn("Dispute created for charge {} (payment intent: {})", charge.getId(), paymentIntentId);
                    if (paymentIntentId != null) {
                        paymentService.handleDispute(paymentIntentId);
                    }
                }
                case "checkout.session.completed" -> {
                    Session session = (Session) stripeObject;
                    log.info("Checkout session completed: {}", session.getId());
                }
                default -> log.info("Unhandled Stripe event type: {}", event.getType());
            }
        } catch (Exception e) {
            log.error("Error processing Stripe event {}: {}", event.getType(), e.getMessage(), e);
        }

        if (eventId != null && !eventId.isBlank()) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
                String requestHash = HexFormat.of().formatHex(hashBytes);

                var idempotencyRecord = IdempotencyKey.builder()
                        .key(eventId)
                        .entity("stripe_webhook")
                        .requestHash(requestHash)
                        .status(IdempotencyStatus.COMPLETED)
                        .expiresAt(LocalDateTime.now().plusHours(72))
                        .build();
                idempotencyKeyRepository.save(idempotencyRecord);
            } catch (NoSuchAlgorithmException e) {
                log.error("SHA-256 not available for webhook hashing", e);
            } catch (Exception e) {
                log.warn("Failed to record webhook idempotency for {}: {}", eventId, e.getMessage());
            }
        }

        return ResponseEntity.ok("Webhook processed");
    }
}
