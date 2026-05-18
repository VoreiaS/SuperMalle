package com.example.superMalle.repository;

import com.example.superMalle.entity.User;
import com.example.superMalle.entity.UserLoyalty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLoyaltyRepository extends JpaRepository<UserLoyalty, Long> {

    Optional<UserLoyalty> findByUser(User user);

    Optional<UserLoyalty> findByUserId(Long userId);

    Optional<UserLoyalty> findByReferralCode(String referralCode);

    List<UserLoyalty> findByLoyaltyProgramId(Long loyaltyProgramId);

    @Query("SELECT ul FROM UserLoyalty ul WHERE ul.isActive = true ORDER BY ul.totalPoints DESC")
    List<UserLoyalty> findTopByTotalPointsDesc(Pageable pageable);

    @Query("SELECT ul FROM UserLoyalty ul WHERE ul.isActive = true AND ul.tierLevel = :tierLevel ORDER BY ul.totalPoints DESC")
    List<UserLoyalty> findByTierLevel(@Param("tierLevel") String tierLevel, Pageable pageable);

    @Query("SELECT COUNT(ul) FROM UserLoyalty ul WHERE ul.isActive = true")
    Long countActiveMembers();

    @Query("SELECT SUM(ul.totalPoints) FROM UserLoyalty ul WHERE ul.isActive = true")
    Long sumTotalPoints();

    @Query("SELECT AVG(ul.totalPoints) FROM UserLoyalty ul WHERE ul.isActive = true")
    Double averagePoints();

    boolean existsByUser(User user);

    boolean existsByUserId(Long userId);

    boolean existsByReferralCode(String referralCode);
}
