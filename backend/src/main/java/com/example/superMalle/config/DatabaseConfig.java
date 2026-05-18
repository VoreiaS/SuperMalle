package com.example.superMalle.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Database Configuration with optimized connection pooling
 * 
 * Uses HikariCP for high-performance connection pooling (PostgreSQL/MySQL)
 * Falls back to DriverManagerDataSource for embedded databases (H2)
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Configuration
@Slf4j
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name:}")
    private String driverClassName;

    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.pool-name:SuperMalleHikariPool}")
    private String poolName;

    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Configuring HikariCP connection pool");
        log.info("  - JDBC URL: {}", jdbcUrl);
        
        HikariConfig config = new HikariConfig();
        
        // Basic configuration
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName(poolName);
        
        // Pool sizing
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        
        // Timeout settings
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        
        // Performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        // Connection testing — skip for H2
        if (!jdbcUrl.contains("h2")) {
            config.setConnectionTestQuery("SELECT 1");
        }
        config.setValidationTimeout(5000);
        
        // Leak detection
        config.setLeakDetectionThreshold(60000);
        
        // Logging
        log.info("HikariCP Pool Configuration:");
        log.info("  - Maximum Pool Size: {}", maximumPoolSize);
        log.info("  - Minimum Idle: {}", minimumIdle);
        log.info("  - Connection Timeout: {}ms", connectionTimeout);
        log.info("  - Idle Timeout: {}ms", idleTimeout);
        log.info("  - Max Lifetime: {}ms", maxLifetime);
        
        return new HikariDataSource(config);
    }
}