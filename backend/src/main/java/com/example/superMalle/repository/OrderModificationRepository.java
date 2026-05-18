package com.example.superMalle.repository;

import com.example.superMalle.entity.Order;
import com.example.superMalle.entity.OrderModification;
import com.example.superMalle.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderModificationRepository extends JpaRepository<OrderModification, Long> {

    List<OrderModification> findByOrderOrderByCreatedAtDesc(Order order);

    Page<OrderModification> findByOrderOrderByCreatedAtDesc(Order order, Pageable pageable);

    List<OrderModification> findByUserOrderByCreatedAtDesc(User user);

    List<OrderModification> findByStatus(String status);

    @Query("SELECT om FROM OrderModification om WHERE om.status = 'PENDING' ORDER BY om.createdAt ASC")
    List<OrderModification> findPendingModifications();

    @Query("SELECT COUNT(om) FROM OrderModification om WHERE om.status = 'PENDING'")
    Long countPendingModifications();

    @Query("SELECT om FROM OrderModification om WHERE om.order = :order AND om.status = 'PENDING'")
    List<OrderModification> findPendingModificationsByOrder(@Param("order") Order order);

    @Query("SELECT om FROM OrderModification om WHERE om.user = :user AND om.status = 'PENDING'")
    List<OrderModification> findPendingModificationsByUser(@Param("user") User user);

    @Query("SELECT COUNT(om) FROM OrderModification om WHERE om.order = :order AND om.status = 'PENDING'")
    Long countPendingModificationsByOrder(@Param("order") Order order);

    @Query("SELECT om FROM OrderModification om WHERE om.modificationType = :type ORDER BY om.createdAt DESC")
    List<OrderModification> findByModificationType(@Param("type") String type);

    @Query("SELECT COUNT(om) FROM OrderModification om WHERE om.status = :status")
    Long countByStatus(@Param("status") String status);
}
