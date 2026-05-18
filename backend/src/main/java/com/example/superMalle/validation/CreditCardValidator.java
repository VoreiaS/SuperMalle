package com.example.superMalle.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Credit card validator implementation
 * 
 * Validates credit card numbers using Luhn algorithm
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
public class CreditCardValidator implements ConstraintValidator<ValidCreditCard, String> {

    @Override
    public void initialize(ValidCreditCard constraintAnnotation) {
        // Initialization if needed
    }

    @Override
    public boolean isValid(String creditCardNumber, ConstraintValidatorContext context) {
        if (creditCardNumber == null || creditCardNumber.isEmpty()) {
            return false;
        }
        
        // Remove spaces and dashes
        String cleaned = creditCardNumber.replaceAll("[\\s\\-]", "");
        
        // Check length (13-19 digits)
        if (cleaned.length() < 13 || cleaned.length() > 19) {
            return false;
        }
        
        // Check if all digits
        if (!cleaned.matches("\\d+")) {
            return false;
        }
        
        // Apply Luhn algorithm
        return luhnCheck(cleaned);
    }

    /**
     * Luhn algorithm implementation
     */
    private boolean luhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10) == 0;
    }
}
