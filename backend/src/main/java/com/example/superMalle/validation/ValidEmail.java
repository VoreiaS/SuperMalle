package com.example.superMalle.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom annotation for validating email addresses
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailValidator.class)
@Documented
public @interface ValidEmail {
    
    String message() default "Invalid email address";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
