package com.example.superMalle.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 * 
 * Configures API documentation with Swagger UI
 * Provides comprehensive API documentation for developers
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Custom OpenAPI configuration
     */
    @Bean
    public OpenAPI superMalleOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SuperMalle Restaurant System API")
                        .description("Comprehensive API documentation for SuperMalle Restaurant System. " +
                                "This API provides endpoints for restaurant management, order processing, " +
                                "user authentication, inventory management, loyalty programs, and more.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SuperMalle Team")
                                .email("support@supermalle.com")
                                .url("https://www.supermalle.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("SuperMalle Documentation")
                        .url("https://docs.supermalle.com"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("https://api.supermalle.com").description("Production Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT authentication token. Obtain from /api/v1/auth/login endpoint"))
                        .addSchemas("ErrorResponse", new Schema<>()
                                .type("object")
                                .addProperty("timestamp", new Schema<>().type("string").format("date-time"))
                                .addProperty("status", new Schema<>().type("integer"))
                                .addProperty("error", new Schema<>().type("string"))
                                .addProperty("message", new Schema<>().type("string"))
                                .addProperty("path", new Schema<>().type("string"))))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }

    /**
     * Grouped OpenAPI for public endpoints
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/v1/auth/**", "/api/v1/menu/**", "/api/v1/categories/**")
                .build();
    }

    /**
     * Grouped OpenAPI for user endpoints
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user")
                .pathsToMatch("/api/v1/users/**", "/api/v1/cart/**", "/api/v1/orders/**")
                .build();
    }

    /**
     * Grouped OpenAPI for admin endpoints
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/api/v1/admin/**", "/api/v1/inventory/**", "/api/v1/loyalty/**")
                .build();
    }

    /**
     * Grouped OpenAPI for payment endpoints
     */
    @Bean
    public GroupedOpenApi paymentApi() {
        return GroupedOpenApi.builder()
                .group("payment")
                .pathsToMatch("/api/v1/payments/**", "/api/v1/stripe/**")
                .build();
    }

    /**
     * Grouped OpenAPI for notification endpoints
     */
    @Bean
    public GroupedOpenApi notificationApi() {
        return GroupedOpenApi.builder()
                .group("notification")
                .pathsToMatch("/api/v1/notifications/**", "/api/v1/emails/**")
                .build();
    }
}
