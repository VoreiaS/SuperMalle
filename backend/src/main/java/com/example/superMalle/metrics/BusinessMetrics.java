package com.example.superMalle.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Business Metrics Collector
 * 
 * Tracks business-level metrics for monitoring and analytics
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Component
@Slf4j
public class BusinessMetrics {

    private final MeterRegistry meterRegistry;

    // Counters
    private final Counter orderCreatedCounter;
    private final Counter orderCompletedCounter;
    private final Counter orderCancelledCounter;
    private final Counter userRegisteredCounter;
    private final Counter loginCounter;
    private final Counter paymentSuccessCounter;
    private final Counter paymentFailureCounter;
    private final Counter emailSentCounter;
    private final Counter emailFailedCounter;
    private final Counter notificationSentCounter;
    private final Counter lowStockAlertCounter;

    // Timers
    private final Timer orderProcessingTimer;
    private final Timer paymentProcessingTimer;
    private final Timer emailSendingTimer;
    private final Timer databaseQueryTimer;
    private final Timer apiResponseTimer;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.orderCreatedCounter = Counter.builder("orders.created")
                .description("Number of orders created")
                .register(meterRegistry);

        this.orderCompletedCounter = Counter.builder("orders.completed")
                .description("Number of orders completed")
                .register(meterRegistry);

        this.orderCancelledCounter = Counter.builder("orders.cancelled")
                .description("Number of orders cancelled")
                .register(meterRegistry);

        this.userRegisteredCounter = Counter.builder("users.registered")
                .description("Number of users registered")
                .register(meterRegistry);

        this.loginCounter = Counter.builder("users.login")
                .description("Number of user logins")
                .register(meterRegistry);

        this.paymentSuccessCounter = Counter.builder("payments.success")
                .description("Number of successful payments")
                .register(meterRegistry);

        this.paymentFailureCounter = Counter.builder("payments.failure")
                .description("Number of failed payments")
                .register(meterRegistry);

        this.emailSentCounter = Counter.builder("emails.sent")
                .description("Number of emails sent")
                .register(meterRegistry);

        this.emailFailedCounter = Counter.builder("emails.failed")
                .description("Number of failed emails")
                .register(meterRegistry);

        this.notificationSentCounter = Counter.builder("notifications.sent")
                .description("Number of notifications sent")
                .register(meterRegistry);

        this.lowStockAlertCounter = Counter.builder("inventory.low_stock_alerts")
                .description("Number of low stock alerts")
                .register(meterRegistry);

        // Initialize timers
        this.orderProcessingTimer = Timer.builder("orders.processing_time")
                .description("Time taken to process orders")
                .register(meterRegistry);

        this.paymentProcessingTimer = Timer.builder("payments.processing_time")
                .description("Time taken to process payments")
                .register(meterRegistry);

        this.emailSendingTimer = Timer.builder("emails.sending_time")
                .description("Time taken to send emails")
                .register(meterRegistry);

        this.databaseQueryTimer = Timer.builder("database.query_time")
                .description("Time taken for database queries")
                .register(meterRegistry);

        this.apiResponseTimer = Timer.builder("api.response_time")
                .description("Time taken for API responses")
                .register(meterRegistry);
    }

    // Counter methods

    public void incrementOrderCreated() {
        orderCreatedCounter.increment();
        log.debug("Order created counter incremented");
    }

    public void incrementOrderCompleted() {
        orderCompletedCounter.increment();
        log.debug("Order completed counter incremented");
    }

    public void incrementOrderCancelled() {
        orderCancelledCounter.increment();
        log.debug("Order cancelled counter incremented");
    }

    public void incrementUserRegistered() {
        userRegisteredCounter.increment();
        log.debug("User registered counter incremented");
    }

    public void incrementLogin() {
        loginCounter.increment();
        log.debug("Login counter incremented");
    }

    public void incrementPaymentSuccess() {
        paymentSuccessCounter.increment();
        log.debug("Payment success counter incremented");
    }

    public void incrementPaymentFailure() {
        paymentFailureCounter.increment();
        log.debug("Payment failure counter incremented");
    }

    public void incrementEmailSent() {
        emailSentCounter.increment();
        log.debug("Email sent counter incremented");
    }

    public void incrementEmailFailed() {
        emailFailedCounter.increment();
        log.debug("Email failed counter incremented");
    }

    public void incrementNotificationSent() {
        notificationSentCounter.increment();
        log.debug("Notification sent counter incremented");
    }

    public void incrementLowStockAlert() {
        lowStockAlertCounter.increment();
        log.debug("Low stock alert counter incremented");
    }

    // Timer methods

    public void recordOrderProcessingTime(long duration, TimeUnit unit) {
        orderProcessingTimer.record(duration, unit);
        log.debug("Order processing time recorded: {} {}", duration, unit);
    }

    public void recordPaymentProcessingTime(long duration, TimeUnit unit) {
        paymentProcessingTimer.record(duration, unit);
        log.debug("Payment processing time recorded: {} {}", duration, unit);
    }

    public void recordEmailSendingTime(long duration, TimeUnit unit) {
        emailSendingTimer.record(duration, unit);
        log.debug("Email sending time recorded: {} {}", duration, unit);
    }

    public void recordDatabaseQueryTime(long duration, TimeUnit unit) {
        databaseQueryTimer.record(duration, unit);
        log.debug("Database query time recorded: {} {}", duration, unit);
    }

    public void recordApiResponseTime(long duration, TimeUnit unit) {
        apiResponseTimer.record(duration, unit);
        log.debug("API response time recorded: {} {}", duration, unit);
    }

    // Timer with Runnable

    public void recordOrderProcessing(Runnable operation) {
        orderProcessingTimer.record(operation);
    }

    public void recordPaymentProcessing(Runnable operation) {
        paymentProcessingTimer.record(operation);
    }

    public void recordEmailSending(Runnable operation) {
        emailSendingTimer.record(operation);
    }

    public void recordDatabaseQuery(Runnable operation) {
        databaseQueryTimer.record(operation);
    }

    public void recordApiResponse(Runnable operation) {
        apiResponseTimer.record(operation);
    }
}
