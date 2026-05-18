package com.example.superMalle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_modification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderModification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "modification_type", nullable = false)
    private String modificationType; // ADD_ITEM, REMOVE_ITEM, UPDATE_QUANTITY, UPDATE_ADDRESS, CANCEL_ITEM

    @Column(name = "previous_value")
    private String previousValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(name = "reason")
    private String reason;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, COMPLETED

    @Column(name = "price_adjustment")
    private Double priceAdjustment;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_by")
    private String rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejected_reason")
    private String rejectedReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Approve the modification
     */
    public void approve(String approvedBy) {
        this.status = "APPROVED";
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * Reject the modification
     */
    public void reject(String rejectedBy, String reason) {
        this.status = "REJECTED";
        this.rejectedBy = rejectedBy;
        this.rejectedAt = LocalDateTime.now();
        this.rejectedReason = reason;
    }

    /**
     * Mark as completed
     */
    public void complete() {
        this.status = "COMPLETED";
    }

    /**
     * Check if modification can be approved
     */
    public boolean canBeApproved() {
        return "PENDING".equals(status);
    }

    /**
     * Check if modification can be rejected
     */
    public boolean canBeRejected() {
        return "PENDING".equals(status);
    }
}
