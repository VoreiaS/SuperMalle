package com.example.superMalle.repository;

import com.example.superMalle.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi.menuItem.id, oi.menuItemName, SUM(oi.quantity), SUM(oi.subtotal) " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.status = com.example.superMalle.entity.enums.OrderStatus.COMPLETED " +
           "GROUP BY oi.menuItem.id, oi.menuItemName " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingItems();
}
