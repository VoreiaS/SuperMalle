package com.example.superMalle.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom annotation for validating credit card numbers
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CreditCardValidator.class)
@Documented
public @interface ValidCreditCard {
    
    String message() default "Invalid credit card number";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
