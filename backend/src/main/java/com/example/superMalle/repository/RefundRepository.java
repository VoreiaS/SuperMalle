package com.example.superMalle.repository;

import com.example.superMalle.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByPaymentId(Long paymentId);

    java.util.Optional<Refund> findByStripeRefundId(String stripeRefundId);
}
