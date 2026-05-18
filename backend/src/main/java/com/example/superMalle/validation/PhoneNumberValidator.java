package com.example.superMalle.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Phone number validator implementation
 * 
 * Validates phone numbers using regex pattern
 * Supports international formats
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    // Supports international phone numbers
    private static final String PHONE_PATTERN = 
            "^\\+?[1-9]\\d{1,14}$";
    
    private static final Pattern pattern = Pattern.compile(PHONE_PATTERN);

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        // Initialization if needed
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        
        // Remove spaces, dashes, and parentheses
        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");
        
        // Check pattern
        return pattern.matcher(cleaned).matches();
    }
}
