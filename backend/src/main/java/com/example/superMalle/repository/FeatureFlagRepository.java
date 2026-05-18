package com.example.superMalle.repository;

import com.example.superMalle.entity.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Feature Flag Repository
 * 
 * Provides database operations for feature flags
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {

    /**
     * Find feature flag by name
     */
    Optional<FeatureFlag> findByName(String name);

    /**
     * Find all enabled feature flags
     */
    List<FeatureFlag> findByEnabledTrue();

    /**
     * Find feature flags by enabled status
     */
    List<FeatureFlag> findByEnabled(Boolean enabled);
}
