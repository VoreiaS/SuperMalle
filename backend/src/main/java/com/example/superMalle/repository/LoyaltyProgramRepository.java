package com.example.superMalle.repository;

import com.example.superMalle.entity.LoyaltyProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyProgramRepository extends JpaRepository<LoyaltyProgram, Long> {

    Optional<LoyaltyProgram> findByIsActiveTrue();

    List<LoyaltyProgram> findByIsActiveTrueOrderByNameAsc();

    Optional<LoyaltyProgram> findByName(String name);

    boolean existsByName(String name);
}
