package com.example.superMalle.annotation;

import java.lang.annotation.*;

/**
 * Audit Log Annotation
 * 
 * Marks methods that should be audited
 * Automatically logs method execution with details
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * Action being performed
     */
    String action();

    /**
     * Entity type being operated on
     */
    String entityType() default "";

    /**
     * Description of the operation
     */
    String description() default "";

    /**
     * Whether to log old and new values
     */
    boolean logValues() default false;

    /**
     * Whether to log request details
     */
    boolean logRequest() default true;
}
