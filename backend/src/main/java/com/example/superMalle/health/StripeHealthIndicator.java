package com.example.superMalle.health;

import com.stripe.Stripe;
import com.stripe.model.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Stripe Health Indicator
 * 
 * Monitors Stripe API connectivity
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StripeHealthIndicator implements HealthIndicator {

    @Value("${app.stripe.secret-key}")
    private String stripeApiKey;

    @Override
    public Health health() {
        try {
            Stripe.apiKey = stripeApiKey;
            
            // Test Stripe connectivity by retrieving account info
            Account account = Account.retrieve();
            
            if (account != null) {
                return Health.up()
                        .withDetail("stripe", "Connected")
                        .withDetail("account_id", account.getId())
                        .withDetail("country", account.getCountry())
                        .withDetail("status", "Active")
                        .build();
            } else {
                return Health.down()
                        .withDetail("error", "Stripe account is null")
                        .build();
            }
        } catch (Exception e) {
            log.error("Stripe health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("stripe", "Disconnected")
                    .build();
        }
    }
}
