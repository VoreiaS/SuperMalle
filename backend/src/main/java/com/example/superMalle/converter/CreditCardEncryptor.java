package com.example.superMalle.converter;

import com.example.superMalle.security.AESUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * JPA AttributeConverter for encrypting/decrypting credit card numbers
 * 
 * Automatically encrypts credit card numbers before storing in database
 * and decrypts them when reading from database
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Converter
@Slf4j
public class CreditCardEncryptor implements AttributeConverter<String, String> {

    private static AESUtil aesUtil;

    /**
     * Set the AES utility (called by Spring)
     */
    @Autowired
    public void setAesUtil(AESUtil aesUtil) {
        CreditCardEncryptor.aesUtil = aesUtil;
    }

    /**
     * Encrypt credit card number before storing in database
     * 
     * @param attribute Plain text credit card number
     * @return Encrypted credit card number
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }

        try {
            String encrypted = aesUtil.encrypt(attribute);
            log.debug("Encrypted credit card number: {}", AESUtil.mask(attribute));
            return encrypted;
        } catch (Exception e) {
            log.error("Failed to encrypt credit card number", e);
            throw new RuntimeException("Failed to encrypt credit card number", e);
        }
    }

    /**
     * Decrypt credit card number when reading from database
     * 
     * @param dbData Encrypted credit card number from database
     * @return Decrypted credit card number
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }

        try {
            String decrypted = aesUtil.decrypt(dbData);
            log.debug("Decrypted credit card number: {}", AESUtil.mask(decrypted));
            return decrypted;
        } catch (Exception e) {
            log.error("Failed to decrypt credit card number", e);
            throw new RuntimeException("Failed to decrypt credit card number", e);
        }
    }
}
