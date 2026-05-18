package com.example.superMalle.converter;

import com.example.superMalle.security.AESUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * JPA AttributeConverter for encrypting/decrypting phone numbers
 * 
 * Automatically encrypts phone numbers before storing in database
 * and decrypts them when reading from database
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Converter
@Slf4j
public class PhoneNumberEncryptor implements AttributeConverter<String, String> {

    private static AESUtil aesUtil;

    /**
     * Set the AES utility (called by Spring)
     */
    @Autowired
    public void setAesUtil(AESUtil aesUtil) {
        PhoneNumberEncryptor.aesUtil = aesUtil;
    }

    /**
     * Encrypt phone number before storing in database
     * 
     * @param attribute Plain text phone number
     * @return Encrypted phone number
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }

        try {
            String encrypted = aesUtil.encrypt(attribute);
            log.debug("Encrypted phone number: {}", AESUtil.mask(attribute));
            return encrypted;
        } catch (Exception e) {
            log.error("Failed to encrypt phone number", e);
            throw new RuntimeException("Failed to encrypt phone number", e);
        }
    }

    /**
     * Decrypt phone number when reading from database
     * 
     * @param dbData Encrypted phone number from database
     * @return Decrypted phone number
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }

        try {
            String decrypted = aesUtil.decrypt(dbData);
            log.debug("Decrypted phone number: {}", AESUtil.mask(decrypted));
            return decrypted;
        } catch (Exception e) {
            log.error("Failed to decrypt phone number", e);
            throw new RuntimeException("Failed to decrypt phone number", e);
        }
    }
}
