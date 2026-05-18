package com.example.superMalle.repository;

import com.example.superMalle.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Audit Log Repository
 * 
 * Provides database operations for audit logs
 * Supports filtering, searching, and pagination
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit logs by user ID
     */
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    /**
     * Find audit logs by action
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);

    /**
     * Find audit logs by entity type
     */
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    /**
     * Find audit logs by correlation ID
     */
    List<AuditLog> findByCorrelationId(String correlationId);

    /**
     * Find audit logs by date range
     */
    Page<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find audit logs by user ID and action
     */
    Page<AuditLog> findByUserIdAndAction(Long userId, String action, Pageable pageable);

    /**
     * Find audit logs by entity type and entity ID
     */
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId);

    /**
     * Search audit logs by description
     */
    @Query("SELECT a FROM AuditLog a WHERE a.description LIKE %:keyword%")
    Page<AuditLog> searchByDescription(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find recent audit logs
     */
    @Query("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC")
    Page<AuditLog> findRecent(Pageable pageable);

    /**
     * Count audit logs by action
     */
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a GROUP BY a.action")
    List<Object[]> countByAction();

    /**
     * Count audit logs by user
     */
    @Query("SELECT a.userId, a.username, COUNT(a) FROM AuditLog a GROUP BY a.userId, a.username")
    List<Object[]> countByUser();

    /**
     * Find failed audit logs
     */
    Page<AuditLog> findByStatus(String status, Pageable pageable);
}
