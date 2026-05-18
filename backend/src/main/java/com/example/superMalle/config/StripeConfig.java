package com.example.superMalle.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class StripeConfig {

    @Value("${app.stripe.secret-key}")
    private String stripeSecretKey;

    @PostConstruct
    public void initStripe() {
        if (stripeSecretKey != null && !stripeSecretKey.isBlank() && stripeSecretKey.startsWith("sk_")) {
            Stripe.apiKey = stripeSecretKey;
            log.info("Stripe API key initialized (key prefix: {})", stripeSecretKey.substring(0, 7));
        } else {
            log.warn("Stripe API key not configured or invalid. Payment features will not work. Set STRIPE_API_KEY env var.");
        }
    }
}
