package com.example.superMalle.repository;

import com.example.superMalle.entity.Payment;
import com.example.superMalle.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
    Page<Payment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
            SELECT p FROM Payment p
            WHERE (:status IS NULL OR p.status = :status)
            AND (:from IS NULL OR p.createdAt >= :from)
            AND (:to IS NULL OR p.createdAt <= :to)
            ORDER BY p.createdAt DESC
            """)
    Page<Payment> findFiltered(@Param("status") PaymentStatus status,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to,
                               Pageable pageable);
}
