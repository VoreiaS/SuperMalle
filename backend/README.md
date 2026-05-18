# SuperMalle Backend

Spring Boot 4.0.5 REST API for the SuperMalle restaurant management system.

```
backend/
├── pom.xml
├── Dockerfile / docker-compose.yml
├── src/main/java/com/example/superMalle/
│   ├── config/          # 18 config classes (security, caching, WS, Stripe, RabbitMQ…)
│   ├── controller/      # 24 REST controllers (public + admin)
│   ├── service/         # 25 service classes with business logic
│   ├── repository/      # 26 JPA repository interfaces
│   ├── entity/          # 28 JPA entities + enums
│   ├── dto/             # 60+ request/response DTOs
│   ├── security/        # JWT, OAuth2, rate limiting
│   ├── exception/       # Global handler + custom exceptions
│   └── health/          # DB / Redis / RabbitMQ / Stripe health indicators
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── db/migration/    # Flyway migrations
│   └── templates/emails/# HTML email templates
└── src/test/            # Unit + integration tests
```

## Quick Start

```bash
./mvnw clean package -DskipTests
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

Dev profile uses H2 in-memory DB and disables Redis/RabbitMQ.

## Key Features

- JWT auth + OAuth2 (Google)
- Stripe payment processing
- Redis caching with per-entity TTL
- RabbitMQ async email/notifications
- Resilience4j circuit breakers + retries
- Bucket4j rate limiting (per-role)
- WebSocket (STOMP) for real-time updates
- Field-level AES encryption for PII
- Flyway database migrations
- Prometheus metrics endpoints
- Structured logging with correlation IDs

## API Reference

See [API_REFERENCE.md](API_REFERENCE.md) for full endpoint documentation.
Swagger UI at `http://localhost:8080/swagger-ui.html`.
