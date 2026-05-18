package com.example.superMalle.repository;

import com.example.superMalle.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    @EntityGraph(attributePaths = "category")
    Page<MenuItem> findByCategoryIdAndIsAvailableTrue(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<MenuItem> findByIsAvailableTrue(Pageable pageable);
    
    @EntityGraph(attributePaths = "category")
    @Query("SELECT m FROM MenuItem m WHERE m.isAvailable = true AND " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<MenuItem> searchAvailableItems(@Param("search") String search, Pageable pageable);
    
    @EntityGraph(attributePaths = "category")
    @Query("SELECT m FROM MenuItem m WHERE m.isAvailable = true AND m.category.id = :categoryId AND " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<MenuItem> searchAvailableItemsByCategory(@Param("categoryId") Long categoryId, @Param("search") String search, Pageable pageable);
    
    @EntityGraph(attributePaths = "category")
    List<MenuItem> findByIsAvailableTrueOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "category")
    @Query("SELECT m FROM MenuItem m WHERE " +
           "(:categoryId IS NULL OR m.category.id = :categoryId) AND " +
           "(:search IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<MenuItem> adminSearch(@Param("search") String search,
                               @Param("categoryId") Long categoryId,
                               Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Optional<MenuItem> findWithCategoryById(Long id);

    @Query("SELECT m FROM MenuItem m")
    @EntityGraph(attributePaths = "category")
    Page<MenuItem> findAllWithCategory(Pageable pageable);
}
