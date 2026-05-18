package com.example.superMalle.repository;

import com.example.superMalle.entity.Inventory;
import com.example.superMalle.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByMenuItem(MenuItem menuItem);

    Optional<Inventory> findByMenuItemId(Long menuItemId);

    List<Inventory> findByIsActiveTrue();

    @Query("SELECT i FROM Inventory i WHERE i.isActive = true AND i.quantity <= i.reorderLevel")
    List<Inventory> findLowStockItems();

    @Query("SELECT i FROM Inventory i WHERE i.isActive = true AND i.quantity <= 0")
    List<Inventory> findOutOfStockItems();

    @Query("SELECT i FROM Inventory i WHERE i.isActive = true AND i.maxQuantity IS NOT NULL AND i.quantity >= i.maxQuantity")
    List<Inventory> findOverstockedItems();

    @Query("SELECT i FROM Inventory i WHERE i.isActive = true AND i.nextRestockDate IS NOT NULL AND i.nextRestockDate <= :date")
    List<Inventory> findItemsNeedingRestock(@Param("date") java.time.LocalDateTime date);

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.isActive = true AND i.quantity <= i.reorderLevel")
    Long countLowStockItems();

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.isActive = true AND i.quantity <= 0")
    Long countOutOfStockItems();

    boolean existsByMenuItem(MenuItem menuItem);
}
