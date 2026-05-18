package com.example.superMalle.service;

import com.example.superMalle.entity.FeatureFlag;
import com.example.superMalle.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Feature Flag Service
 * 
 * Manages feature flags for controlled feature rollouts
 * Supports percentage-based rollouts and user targeting
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;
    private final Random random = new Random();

    /**
     * Check if a feature is enabled for a user
     */
    @Cacheable(value = "featureFlags", key = "#featureName + ':' + #userId")
    public boolean isFeatureEnabled(String featureName, Long userId) {
        Optional<FeatureFlag> featureFlagOpt = featureFlagRepository.findByName(featureName);
        
        if (featureFlagOpt.isEmpty()) {
            log.warn("Feature flag not found: {}", featureName);
            return false;
        }

        FeatureFlag featureFlag = featureFlagOpt.get();

        if (!featureFlag.getEnabled()) {
            log.debug("Feature flag disabled: {}", featureName);
            return false;
        }

        // Check if user is in target list
        if (userId != null && featureFlag.getTargetUserIds() != null) {
            List<Long> targetUserIds = Arrays.stream(featureFlag.getTargetUserIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            if (targetUserIds.contains(userId)) {
                log.debug("User {} is in target list for feature {}", userId, featureName);
                return true;
            }
        }

        // Check percentage-based rollout
        if (featureFlag.getRolloutPercentage() < 100) {
            int hash = (featureName + userId).hashCode();
            int percentage = Math.abs(hash % 100);
            boolean enabled = percentage < featureFlag.getRolloutPercentage();
            
            log.debug("Feature {} rollout check: userId={}, percentage={}, enabled={}", 
                    featureName, userId, featureFlag.getRolloutPercentage(), enabled);
            
            return enabled;
        }

        log.debug("Feature {} enabled for user {}", featureName, userId);
        return true;
    }

    /**
     * Check if a feature is enabled (without user context)
     */
    @Cacheable(value = "featureFlags", key = "#featureName")
    public boolean isFeatureEnabled(String featureName) {
        Optional<FeatureFlag> featureFlagOpt = featureFlagRepository.findByName(featureName);
        
        if (featureFlagOpt.isEmpty()) {
            log.warn("Feature flag not found: {}", featureName);
            return false;
        }

        FeatureFlag featureFlag = featureFlagOpt.get();
        boolean enabled = featureFlag.getEnabled();
        
        log.debug("Feature {} enabled: {}", featureName, enabled);
        return enabled;
    }

    /**
     * Get all feature flags
     */
    public List<FeatureFlag> getAllFeatureFlags() {
        return featureFlagRepository.findAll();
    }

    /**
     * Get enabled feature flags
     */
    public List<FeatureFlag> getEnabledFeatureFlags() {
        return featureFlagRepository.findByEnabledTrue();
    }

    /**
     * Get feature flag by name
     */
    public Optional<FeatureFlag> getFeatureFlag(String name) {
        return featureFlagRepository.findByName(name);
    }

    /**
     * Create feature flag
     */
    @CacheEvict(value = "featureFlags", allEntries = true)
    public FeatureFlag createFeatureFlag(FeatureFlag featureFlag) {
        log.info("Creating feature flag: {}", featureFlag.getName());
        return featureFlagRepository.save(featureFlag);
    }

    /**
     * Update feature flag
     */
    @CacheEvict(value = "featureFlags", allEntries = true)
    public FeatureFlag updateFeatureFlag(Long id, FeatureFlag featureFlag) {
        log.info("Updating feature flag: {}", id);
        featureFlag.setId(id);
        return featureFlagRepository.save(featureFlag);
    }

    /**
     * Delete feature flag
     */
    @CacheEvict(value = "featureFlags", allEntries = true)
    public void deleteFeatureFlag(Long id) {
        log.info("Deleting feature flag: {}", id);
        featureFlagRepository.deleteById(id);
    }

    /**
     * Enable feature flag
     */
    @CacheEvict(value = "featureFlags", allEntries = true)
    public FeatureFlag enableFeatureFlag(String name) {
        log.info("Enabling feature flag: {}", name);
        FeatureFlag featureFlag = featureFlagRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Feature flag not found: " + name));
        featureFlag.setEnabled(true);
        return featureFlagRepository.save(featureFlag);
    }

    /**
     * Disable feature flag
     */
    @CacheEvict(value = "featureFlags", allEntries = true)
    public FeatureFlag disableFeatureFlag(String name) {
        log.info("Disabling feature flag: {}", name);
        FeatureFlag featureFlag = featureFlagRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Feature flag not found: " + name));
        featureFlag.setEnabled(false);
        return featureFlagRepository.save(featureFlag);
    }

    /**
     * Update rollout percentage
     */
    @CacheEvict(value = "featureFlags", allEntries = true)
    public FeatureFlag updateRolloutPercentage(String name, int percentage) {
        log.info("Updating rollout percentage for feature {}: {}%", name, percentage);
        FeatureFlag featureFlag = featureFlagRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Feature flag not found: " + name));
        featureFlag.setRolloutPercentage(percentage);
        return featureFlagRepository.save(featureFlag);
    }
}
