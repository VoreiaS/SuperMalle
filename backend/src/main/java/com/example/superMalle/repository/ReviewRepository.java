package com.example.superMalle.repository;

import com.example.superMalle.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByMenuItemId(Long menuItemId, Pageable pageable);
    Page<Review> findByUserId(Long userId, Pageable pageable);
    Page<Review> findByIsApprovedFalse(Pageable pageable);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.menuItem.id = :menuItemId")
    Double findAverageRatingByMenuItemId(@Param("menuItemId") Long menuItemId);
    
    long countByMenuItemId(Long menuItemId);

    java.util.Optional<Review> findByOrderIdAndUserId(Long orderId, Long userId);
}
