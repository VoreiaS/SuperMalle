package com.example.superMalle.repository;

import com.example.superMalle.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    Optional<Coupon> findByCodeAndIsActiveTrue(String code);
    
    boolean existsByCode(String code);
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.deleted = false ORDER BY c.createdAt DESC")
    Page<Coupon> findByIsActiveTrueAndDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT c FROM Coupon c WHERE c.deleted = false ORDER BY c.createdAt DESC")
    Page<Coupon> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT c FROM Coupon c WHERE c.deleted = false AND c.code = :code")
    Optional<Coupon> findByCodeAndNotDeleted(@Param("code") String code);
}
