package com.example.superMalle.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private String stripePaymentIntentId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethodType;
    private String cardLast4;
    private String cardBrand;
    private String receiptUrl;
    private LocalDateTime createdAt;
    private List<RefundResponse> refunds;
}
