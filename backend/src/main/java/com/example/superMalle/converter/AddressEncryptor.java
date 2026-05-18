package com.example.superMalle.converter;

import com.example.superMalle.security.AESUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * JPA AttributeConverter for encrypting/decrypting addresses
 * 
 * Automatically encrypts addresses before storing in database
 * and decrypts them when reading from database
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Converter
@Slf4j
public class AddressEncryptor implements AttributeConverter<String, String> {

    private static AESUtil aesUtil;

    /**
     * Set the AES utility (called by Spring)
     */
    @Autowired
    public void setAesUtil(AESUtil aesUtil) {
        AddressEncryptor.aesUtil = aesUtil;
    }

    /**
     * Encrypt address before storing in database
     * 
     * @param attribute Plain text address
     * @return Encrypted address
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }

        try {
            String encrypted = aesUtil.encrypt(attribute);
            log.debug("Encrypted address");
            return encrypted;
        } catch (Exception e) {
            log.error("Failed to encrypt address", e);
            throw new RuntimeException("Failed to encrypt address", e);
        }
    }

    /**
     * Decrypt address when reading from database
     * 
     * @param dbData Encrypted address from database
     * @return Decrypted address
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }

        try {
            String decrypted = aesUtil.decrypt(dbData);
            log.debug("Decrypted address");
            return decrypted;
        } catch (Exception e) {
            log.error("Failed to decrypt address", e);
            throw new RuntimeException("Failed to decrypt address", e);
        }
    }
}
