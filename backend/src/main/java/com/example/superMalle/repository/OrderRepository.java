package com.example.superMalle.repository;

import com.example.superMalle.entity.Order;
import com.example.superMalle.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:from IS NULL OR o.createdAt >= :from) AND " +
           "(:to IS NULL OR o.createdAt <= :to)")
    Page<Order> findWithFilters(@Param("status") OrderStatus status,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to,
                                Pageable pageable);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :from AND :to")
    long countByStatusAndCreatedAtBetween(@Param("status") OrderStatus status,
                                           @Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :from AND :to")
    BigDecimal sumCompletedOrderAmountBetween(@Param("status") OrderStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :from AND :to")
    BigDecimal sumCompletedRevenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
    long countByCreatedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status NOT IN ('COMPLETED', 'CANCELLED')")
    long countActiveOrders();
    
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    List<Order> findByUserId(Long userId);

    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.paymentStatus = 'PENDING' AND o.createdAt < :cutoff AND o.cancellationReason IS NULL")
    List<Order> findUnpaidPendingOrdersOlderThan(@Param("cutoff") LocalDateTime cutoff);

    @Query(value = """
        SELECT DATE(o.created_at) as date,
               CAST(COUNT(o.id) AS long) as orderCount,
               COALESCE(SUM(o.total_amount), 0) as revenue
        FROM orders o
        WHERE o.status = 'COMPLETED'
          AND o.created_at BETWEEN :from AND :to
        GROUP BY DATE(o.created_at)
        ORDER BY DATE(o.created_at)
        """, nativeQuery = true)
    List<Object[]> findDailySalesReport(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
