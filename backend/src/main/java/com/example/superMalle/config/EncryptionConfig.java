package com.example.superMalle.config;

import com.example.superMalle.converter.AddressEncryptor;
import com.example.superMalle.converter.CreditCardEncryptor;
import com.example.superMalle.converter.PhoneNumberEncryptor;
import com.example.superMalle.security.AESUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Encryption Configuration
 * 
 * Configures encryption utilities and converters for sensitive data
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Configuration
@Slf4j
public class EncryptionConfig {

    /**
     * Create AES encryption utility bean
     */
    @Bean
    public AESUtil aesUtil() {
        log.info("Initializing AES encryption utility");
        return new AESUtil();
    }

    /**
     * Create credit card encryptor bean
     */
    @Bean
    public CreditCardEncryptor creditCardEncryptor(AESUtil aesUtil) {
        log.info("Initializing credit card encryptor");
        CreditCardEncryptor encryptor = new CreditCardEncryptor();
        encryptor.setAesUtil(aesUtil);
        return encryptor;
    }

    /**
     * Create address encryptor bean
     */
    @Bean
    public AddressEncryptor addressEncryptor(AESUtil aesUtil) {
        log.info("Initializing address encryptor");
        AddressEncryptor encryptor = new AddressEncryptor();
        encryptor.setAesUtil(aesUtil);
        return encryptor;
    }

    /**
     * Create phone number encryptor bean
     */
    @Bean
    public PhoneNumberEncryptor phoneNumberEncryptor(AESUtil aesUtil) {
        log.info("Initializing phone number encryptor");
        PhoneNumberEncryptor encryptor = new PhoneNumberEncryptor();
        encryptor.setAesUtil(aesUtil);
        return encryptor;
    }
}
