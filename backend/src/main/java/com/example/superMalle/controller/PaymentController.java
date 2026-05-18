package com.example.superMalle.controller;

import com.example.superMalle.dto.payment.CreatePaymentIntentRequest;
import com.example.superMalle.dto.payment.PaymentIntentResponse;
import com.example.superMalle.dto.payment.PaymentResponse;
import com.example.superMalle.security.CustomUserDetails;
import com.example.superMalle.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.ok(paymentService.createPaymentIntent(
                request.getOrderId(),
                request.getPaymentMethodType(),
                userDetails.getUsername(),
                idempotencyKey
        ));
    }

    @GetMapping("/order/{orderId:\\d+}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId, userDetails.getUsername()));
    }
}
