package com.example.superMalle.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Validates that no placeholder secrets are used in production.
 * The application will fail to start if placeholder values are detected
 * when running with the "postgres" profile (i.e., production-like).
 */
@Configuration
@Slf4j
public class SecretValidationConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${app.stripe.webhook-secret}")
    private String stripeWebhookSecret;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @PostConstruct
    public void validateSecrets() {
        if (activeProfile.contains("postgres")) {
            // Production profile - strict validation
            if (jwtSecret.contains("change-this-secret-key") || jwtSecret.contains("change-in-production")) {
                throw new IllegalStateException(
                    "SECURITY: JWT secret contains placeholder value. " +
                    "Set the JWT_SECRET environment variable to a strong random key (at least 32 characters)."
                );
            }
            if (stripeSecretKey.contains("placeholder") || stripeSecretKey.startsWith("sk_tes...")) {
                throw new IllegalStateException(
                    "SECURITY: Stripe secret key contains placeholder value. " +
                    "Set the STRIPE_API_KEY environment variable."
                );
            }
            if (stripeWebhookSecret.contains("placeholder") || stripeWebhookSecret.startsWith("whsec_placeholder")) {
                throw new IllegalStateException(
                    "SECURITY: Stripe webhook secret contains placeholder value. " +
                    "Set the STRIPE_WEBHOOK_SECRET environment variable."
                );
            }
            log.info("Secret validation passed for production profile");
        } else {
            // Dev profile - warn but don't block
            if (jwtSecret.contains("change-this-secret-key")) {
                log.warn("WARNING: Using default JWT secret. This is OK for development but MUST be changed in production.");
            }
            if (stripeSecretKey.startsWith("sk_tes...")) {
                log.warn("WARNING: Using placeholder Stripe key. Payment features will not work until STRIPE_API_KEY is set.");
            }
        }
    }
}
