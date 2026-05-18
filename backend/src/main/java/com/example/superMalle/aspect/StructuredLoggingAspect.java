package com.example.superMalle.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Structured Logging Aspect
 * 
 * Provides structured logging for method calls
 * Logs method entry, exit, and exceptions with correlation IDs
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Aspect
@Component
@Slf4j
public class StructuredLoggingAspect {

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceMethods() {}

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void repositoryMethods() {}

    @Around("serviceMethods() || controllerMethods() || repositoryMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String correlationId = MDC.get("correlationId");
        
        long startTime = System.currentTimeMillis();
        
        log.info("Method started: {}.{} - correlationId: {}, args: {}", 
                className, methodName, correlationId, Arrays.toString(joinPoint.getArgs()));
        
        try {
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("Method completed: {}.{} - correlationId: {}, duration: {}ms", 
                    className, methodName, correlationId, duration);
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            log.error("Method failed: {}.{} - correlationId: {}, duration: {}ms, error: {}", 
                    className, methodName, correlationId, duration, e.getMessage(), e);
            
            throw e;
        }
    }
}
