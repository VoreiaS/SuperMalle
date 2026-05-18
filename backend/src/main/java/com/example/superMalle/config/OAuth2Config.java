package com.example.superMalle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
public class OAuth2Config {

    /** Only create the repository when Google credentials are actually set. */
    public static class GoogleCredentialsPresent implements Condition {
        @Override
        public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata meta) {
            String clientId = ctx.getEnvironment().getProperty("app.oauth2.google.client-id");
            String clientSecret = ctx.getEnvironment().getProperty("app.oauth2.google.client-secret");
            return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
        }
    }

    @Bean
    @Conditional(GoogleCredentialsPresent.class)
    public ClientRegistrationRepository clientRegistrationRepository(
            org.springframework.core.env.Environment env) {
        return new InMemoryClientRegistrationRepository(
            ClientRegistration.withRegistrationId("google")
                .clientId(env.getProperty("app.oauth2.google.client-id"))
                .clientSecret(env.getProperty("app.oauth2.google.client-secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/api/v1/oauth2/callback/{registrationId}")
                .scope("email", "profile")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("email")
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .clientName("Google")
                .build()
        );
    }
}
