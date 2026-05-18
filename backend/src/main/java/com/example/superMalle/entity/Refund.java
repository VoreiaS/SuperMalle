package com.example.superMalle.entity;

import com.example.superMalle.entity.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    @ToString.Exclude
    private Payment payment;

    private String stripeRefundId;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal amount;

    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private RefundStatus status = RefundStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
