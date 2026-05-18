package com.example.superMalle.entity;

import com.example.superMalle.entity.enums.OrderStatus;
import com.example.superMalle.entity.enums.OrderType;
import com.example.superMalle.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user_id", columnList = "user_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"items", "statusLog", "payment"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    @ToString.Exclude
    private User user;

    @Column(unique = true, length = 30)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    @NotNull
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(precision = 12, scale = 4)
    private BigDecimal subtotalAmount;

    @Column(precision = 12, scale = 4)
    private BigDecimal taxAmount;

    @Column(precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal tipAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 4)
    @NotNull
    private BigDecimal totalAmount;

    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(length = 500)
    private String deliveryAddress;

    @Column(precision = 12, scale = 4)
    private BigDecimal deliveryCharge;

    private String specialInstructions;

    private String couponCode;

    @Column(precision = 12, scale = 4)
    private BigDecimal discountAmount;

    private String cancellationReason;

    private LocalDateTime estimatedReadyAt;

    private LocalDateTime completedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<OrderItem> items = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<OrderStatusLog> statusLog = new java.util.ArrayList<>();

    @OneToOne(mappedBy = "order")
    @ToString.Exclude
    private Payment payment;

    @PrePersist
    private void generateOrderNumber() {
        if (this.orderNumber == null) {
            this.orderNumber = "ORD-" + System.currentTimeMillis() + "-" +
                    ThreadLocalRandom.current().nextInt(100, 999);
        }
    }
}
