package com.example.superMalle.entity;

import com.example.superMalle.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"refunds"})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id")
    @ToString.Exclude
    private Order order;

    @Column(unique = true)
    private String stripePaymentIntentId;

    private String stripePaymentMethodId;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal amount;

    @Builder.Default
    private String currency = "usd";

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    private String paymentMethodType;

    @Column(length = 4)
    private String cardLast4;

    private String cardBrand;

    private String receiptUrl;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    /** Idempotency key for duplicate request prevention */
    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;

    /** Client IP for audit/logging */
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    /** User agent string for audit */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /** When the payment was actually processed */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Refund> refunds = new java.util.ArrayList<>();
}
