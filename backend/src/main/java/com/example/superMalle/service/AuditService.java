package com.example.superMalle.service;

import com.example.superMalle.entity.AuditLog;
import com.example.superMalle.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Audit Service
 * 
 * Handles audit logging for all system operations
 * Provides automatic audit logging via AOP
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Aspect
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Audit log aspect
     * Intercepts methods annotated with @AuditLog
     */
    @Around("@annotation(com.example.superMalle.annotation.AuditLog)")
    public Object auditMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        com.example.superMalle.annotation.AuditLog annotation = 
            method.getAnnotation(com.example.superMalle.annotation.AuditLog.class);

        String correlationId = MDC.get("correlationId");
        long startTime = System.currentTimeMillis();

        AuditLog auditLog = AuditLog.builder()
                .action(annotation.action())
                .entityType(annotation.entityType())
                .description(annotation.description())
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .status("SUCCESS")
                .build();

        // Extract user information if available
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setRequestMethod(request.getMethod());
                auditLog.setRequestUri(request.getRequestURI());

                // Extract user ID from request if authenticated
                Object userId = request.getAttribute("userId");
                if (userId != null) {
                    auditLog.setUserId(Long.valueOf(userId.toString()));
                }
                Object username = request.getAttribute("username");
                if (username != null) {
                    auditLog.setUsername(username.toString());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract request information for audit log", e);
        }

        // Log method arguments if enabled
        if (annotation.logRequest()) {
            auditLog.setOldValue(Arrays.toString(joinPoint.getArgs()));
        }

        try {
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            auditLog.setDurationMs(duration);
            
            // Log result if enabled
            if (annotation.logValues() && result != null) {
                auditLog.setNewValue(result.toString());
            }

            // Save audit log asynchronously
            saveAuditLogAsync(auditLog);

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            auditLog.setDurationMs(duration);
            auditLog.setStatus("FAILED");
            auditLog.setErrorMessage(e.getMessage());

            // Save audit log asynchronously
            saveAuditLogAsync(auditLog);

            throw e;
        }
    }

    /**
     * Save audit log asynchronously
     */
    @Async
    public void saveAuditLogAsync(AuditLog auditLog) {
        try {
            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: action={}, correlationId={}", auditLog.getAction(), auditLog.getCorrelationId());
        } catch (Exception e) {
            log.error("Failed to save audit log: action={}, correlationId={}", 
                    auditLog.getAction(), auditLog.getCorrelationId(), e);
        }
    }

    /**
     * Create manual audit log entry
     */
    @Async
    public void createAuditLog(String action, String entityType, String entityId, String description, 
                              Long userId, String username, String correlationId) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .userId(userId)
                .username(username)
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .status("SUCCESS")
                .build();

        saveAuditLogAsync(auditLog);
    }

    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
