package com.example.superMalle.repository;

import com.example.superMalle.entity.LoyaltyTransaction;
import com.example.superMalle.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    List<LoyaltyTransaction> findByUserOrderByCreatedAtDesc(User user);

    Page<LoyaltyTransaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<LoyaltyTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<LoyaltyTransaction> findByTransactionTypeOrderByCreatedAtDesc(String transactionType);

    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.user = :user AND lt.createdAt BETWEEN :startDate AND :endDate ORDER BY lt.createdAt DESC")
    List<LoyaltyTransaction> findByUserAndDateRange(@Param("user") User user, 
                                                      @Param("startDate") LocalDateTime startDate, 
                                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.transactionType = :type AND lt.createdAt BETWEEN :startDate AND :endDate")
    List<LoyaltyTransaction> findByTransactionTypeAndDateRange(@Param("type") String type,
                                                                @Param("startDate") LocalDateTime startDate,
                                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(lt) FROM LoyaltyTransaction lt WHERE lt.user = :user AND lt.transactionType = :type")
    Long countByUserAndTransactionType(@Param("user") User user, @Param("type") String type);

    @Query("SELECT SUM(lt.points) FROM LoyaltyTransaction lt WHERE lt.user = :user AND lt.transactionType = :type")
    Long sumPointsByUserAndTransactionType(@Param("user") User user, @Param("type") String type);

    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.referenceId = :referenceId")
    List<LoyaltyTransaction> findByReferenceId(@Param("referenceId") String referenceId);
}
