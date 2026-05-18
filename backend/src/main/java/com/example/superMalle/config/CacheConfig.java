package com.example.superMalle.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration
 * 
 * Provides multi-level caching strategy with different TTLs for different data types:
 * - Short-term cache: 5 minutes (frequently changing data)
 * - Medium-term cache: 30 minutes (moderately changing data)
 * - Long-term cache: 2 hours (rarely changing data)
 */
@Configuration
@EnableCaching
@Slf4j
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true", matchIfMissing = true)
public class CacheConfig {

    public static final String MENU_ITEMS_CACHE = "menuItems";
    public static final String CATEGORIES_CACHE = "categories";
    public static final String LOYALTY_PROGRAM_CACHE = "loyaltyProgram";
    public static final String USER_LOYALTY_CACHE = "userLoyalty";
    public static final String INVENTORY_CACHE = "inventory";
    public static final String COUPONS_CACHE = "coupons";
    public static final String SETTINGS_CACHE = "settings";

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("Initializing Redis Cache Manager with custom TTL configurations");

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new JdkSerializationRedisSerializer()))
                .disableCachingNullValues();

        // Custom cache configurations with different TTLs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Menu items - Long cache (2 hours) - rarely changes
        cacheConfigurations.put(MENU_ITEMS_CACHE, defaultConfig
                .entryTtl(Duration.ofHours(2)));

        // Categories - Long cache (2 hours) - rarely changes
        cacheConfigurations.put(CATEGORIES_CACHE, defaultConfig
                .entryTtl(Duration.ofHours(2)));

        // Loyalty program - Medium cache (1 hour) - changes occasionally
        cacheConfigurations.put(LOYALTY_PROGRAM_CACHE, defaultConfig
                .entryTtl(Duration.ofHours(1)));

        // User loyalty - Short cache (10 minutes) - changes frequently
        cacheConfigurations.put(USER_LOYALTY_CACHE, defaultConfig
                .entryTtl(Duration.ofMinutes(10)));

        // Inventory - Short cache (5 minutes) - changes frequently
        cacheConfigurations.put(INVENTORY_CACHE, defaultConfig
                .entryTtl(Duration.ofMinutes(5)));

        // Coupons - Medium cache (30 minutes) - changes occasionally
        cacheConfigurations.put(COUPONS_CACHE, defaultConfig
                .entryTtl(Duration.ofMinutes(30)));

        // Settings - Long cache (2 hours) - rarely changes
        cacheConfigurations.put(SETTINGS_CACHE, defaultConfig
                .entryTtl(Duration.ofHours(2)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new JdkSerializationRedisSerializer()));
    }
}
