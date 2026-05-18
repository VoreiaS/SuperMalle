package com.example.superMalle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_transaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loyalty_program_id", nullable = false)
    private LoyaltyProgram loyaltyProgram;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // EARNED, REDEEMED, REFERRAL_BONUS, WELCOME_BONUS, ADJUSTMENT

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "balance_before", nullable = false)
    private Integer balanceBefore;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "description")
    private String description;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Create an earned points transaction
     */
    public static LoyaltyTransaction createEarnedTransaction(User user, LoyaltyProgram program, Order order, Integer points, Integer balanceBefore, Integer balanceAfter) {
        return LoyaltyTransaction.builder()
                .user(user)
                .loyaltyProgram(program)
                .order(order)
                .transactionType("EARNED")
                .points(points)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .description("Points earned from order #" + (order != null ? order.getOrderNumber() : "N/A"))
                .referenceId(order != null ? order.getId().toString() : null)
                .build();
    }

    /**
     * Create a redeemed points transaction
     */
    public static LoyaltyTransaction createRedeemedTransaction(User user, LoyaltyProgram program, Order order, Integer points, Integer balanceBefore, Integer balanceAfter) {
        return LoyaltyTransaction.builder()
                .user(user)
                .loyaltyProgram(program)
                .order(order)
                .transactionType("REDEEMED")
                .points(-points)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .description("Points redeemed for order #" + (order != null ? order.getOrderNumber() : "N/A"))
                .referenceId(order != null ? order.getId().toString() : null)
                .build();
    }

    /**
     * Create a referral bonus transaction
     */
    public static LoyaltyTransaction createReferralBonusTransaction(User user, LoyaltyProgram program, Integer points, Integer balanceBefore, Integer balanceAfter, String referralCode) {
        return LoyaltyTransaction.builder()
                .user(user)
                .loyaltyProgram(program)
                .transactionType("REFERRAL_BONUS")
                .points(points)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .description("Referral bonus for referring new user")
                .referenceId(referralCode)
                .build();
    }

    /**
     * Create a welcome bonus transaction
     */
    public static LoyaltyTransaction createWelcomeBonusTransaction(User user, LoyaltyProgram program, Integer points, Integer balanceBefore, Integer balanceAfter) {
        return LoyaltyTransaction.builder()
                .user(user)
                .loyaltyProgram(program)
                .transactionType("WELCOME_BONUS")
                .points(points)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .description("Welcome bonus points")
                .build();
    }
}
