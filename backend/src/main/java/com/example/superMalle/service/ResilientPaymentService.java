package com.example.superMalle.service;

import com.example.superMalle.config.ResilienceConfig;
import com.example.superMalle.exception.PaymentException;
import com.example.superMalle.exception.ServiceUnavailableException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResilientPaymentService {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final TimeLimiter stripeTimeLimiter;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public PaymentIntent createPaymentIntent(
            BigDecimal amount,
            String currency,
            String customerId,
            Long orderId,
            String idempotencyKey) {
        log.info("Creating payment intent: amount={}, currency={}, customerId={}, orderId={}",
                amount, currency, customerId, orderId);

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(ResilienceConfig.STRIPE_CIRCUIT_BREAKER);
        Retry retry = retryRegistry.retry(ResilienceConfig.STRIPE_RETRY);

        Supplier<PaymentIntent> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
                Retry.decorateSupplier(retry,
                        () -> createPaymentIntentInternal(amount, currency, customerId, orderId, idempotencyKey)));

        try {
            PaymentIntent paymentIntent = supplier.get();
            log.info("Payment intent created successfully: {}", paymentIntent.getId());
            return paymentIntent;
        } catch (Exception e) {
            log.error("Failed to create payment intent", e);
            throw new PaymentException("Failed to create payment intent: " + e.getMessage(), e);
        }
    }

    public PaymentIntent createPaymentIntentWithTimeout(
            BigDecimal amount,
            String currency,
            String customerId,
            Long orderId,
            String idempotencyKey) {
        log.info("Creating payment intent with timeout: amount={}, currency={}, customerId={}, orderId={}",
                amount, currency, customerId, orderId);

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(ResilienceConfig.STRIPE_CIRCUIT_BREAKER);

        Supplier<CompletableFuture<PaymentIntent>> futureSupplier = () ->
                CompletableFuture.supplyAsync(
                        () -> createPaymentIntentInternal(amount, currency, customerId, orderId, idempotencyKey),
                        executorService);

        try {
            PaymentIntent paymentIntent = stripeTimeLimiter.executeFutureSupplier(futureSupplier);
            log.info("Payment intent created successfully with timeout: {}", paymentIntent.getId());
            return paymentIntent;
        } catch (TimeoutException e) {
            log.error("Payment intent creation timed out", e);
            throw new ServiceUnavailableException("Payment service timeout", e);
        } catch (Exception e) {
            log.error("Failed to create payment intent with timeout", e);
            throw new PaymentException("Failed to create payment intent: " + e.getMessage(), e);
        }
    }

    private PaymentIntent createPaymentIntentInternal(
            BigDecimal amount,
            String currency,
            String customerId,
            Long orderId,
            String idempotencyKey) {
        log.debug("Calling Stripe API to create payment intent for order {}", orderId);

        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency.toLowerCase())
                .setCustomer(customerId)
                .addPaymentMethodType("card")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .putMetadata("order_id", orderId != null ? orderId.toString() : "")
                .setReceiptEmail(null);

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            paramsBuilder.putMetadata("idempotency_key", idempotencyKey);
        }

        PaymentIntentCreateParams params = paramsBuilder.build();

        try {
            return PaymentIntent.create(params);
        } catch (Exception e) {
            log.error("Stripe API call failed for order {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Stripe API error: " + e.getMessage(), e);
        }
    }
}
