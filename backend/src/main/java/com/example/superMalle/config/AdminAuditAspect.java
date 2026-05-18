package com.example.superMalle.config;

import com.example.superMalle.entity.AuditLog;
import com.example.superMalle.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * AOP Aspect for auditing all admin controller actions.
 * Logs: admin email, action method, status, IP address, timestamp, and any errors.
 * Essential for compliance, security monitoring, and operational debugging.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AdminAuditAspect {

    private final AuditLogRepository auditLogRepository;

    @Pointcut("within(com.example.superMalle.controller.admin..*)")
    public void adminControllerMethods() {}

    @AfterReturning(pointcut = "adminControllerMethods()", returning = "result")
    public void logAdminSuccess(JoinPoint joinPoint, Object result) {
        logAdminAction(joinPoint, "SUCCESS", null);
    }

    @AfterThrowing(pointcut = "adminControllerMethods()", throwing = "ex")
    public void logAdminError(JoinPoint joinPoint, Throwable ex) {
        logAdminAction(joinPoint, "ERROR", ex.getMessage());
    }

    private void logAdminAction(JoinPoint joinPoint, String status, String errorDetail) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String adminEmail = auth != null && auth.isAuthenticated() ? auth.getName() : "anonymous";

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attrs != null ? attrs.getRequest() : null;

            String methodName = joinPoint.getSignature().toShortString();
            String ipAddress = request != null ? request.getRemoteAddr() : "unknown";
            String userAgent = request != null ? request.getHeader("User-Agent") : "unknown";

            AuditLog logEntry = AuditLog.builder()
                .username(adminEmail)
                .action(methodName)
                .status(status)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .errorMessage(errorDetail)
                .build();

            auditLogRepository.save(logEntry);
            log.info("Admin audit: {} by {} - {}", methodName, adminEmail, status);
        } catch (Exception e) {
            // Never let audit logging failures break the main request
            log.warn("Failed to write audit log for action: {}", joinPoint.getSignature(), e);
        }
    }
}
