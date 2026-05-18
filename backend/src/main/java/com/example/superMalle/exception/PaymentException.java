package com.example.superMalle.exception;

/**
 * Exception thrown when payment operations fail
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
public class PaymentException extends RuntimeException {

    private final String paymentId;

    public PaymentException(String message) {
        super(message);
        this.paymentId = null;
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.paymentId = null;
    }

    public PaymentException(String paymentId, String message) {
        super(message);
        this.paymentId = paymentId;
    }

    public PaymentException(String paymentId, String message, Throwable cause) {
        super(message, cause);
        this.paymentId = paymentId;
    }

    public String getPaymentId() {
        return paymentId;
    }
}
