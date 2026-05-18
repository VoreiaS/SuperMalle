# SuperMalle Backend

Spring Boot 4.0.5 REST API for the SuperMalle restaurant management system. Built with Java 21, PostgreSQL, Redis, RabbitMQ, and Stripe integration.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Database](#database)
- [Caching Strategy](#caching-strategy)
- [Security](#security)
- [Monitoring](#monitoring)
- [Docker](#docker)
- [Testing](#testing)
- [API Reference](#api-reference)

---

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Core language |
| Spring Boot | 4.0.5 | Application framework |
| Spring Security | 6.x | Authentication & authorization |
| Spring Data JPA | 3.x | Database ORM |
| Spring WebSocket | - | Real-time STOMP messaging |
| PostgreSQL | 15 | Primary database |
| H2 | - | Dev/test in-memory database |
| Redis | 7 | Caching layer |
| RabbitMQ | - | Async message queuing |
| Stripe Java | 28.4.0 | Payment processing |
| JWT (jjwt) | 0.12.6 | Token authentication |
| Resilience4j | 2.2.0 | Circuit breaker, retry, time limiter |
| Bucket4j | 8.10.1 | Rate limiting |
| SpringDoc OpenAPI | 2.8.6 | Swagger UI documentation |
| Lombok | - | Boilerplate reduction |
| Flyway | - | Database migrations |
| Micrometer + Prometheus | - | Metrics export |
| Thymeleaf | - | Email templates |

---

## Project Structure

```
src/main/java/com/example/superMalle/
├── SuperMalleApplication.java          # Entry point
│
├── config/                             # Spring configuration classes (18 files)
│   ├── SecurityConfig.java             # JWT auth, OAuth2, CORS, CSRF
│   ├── CacheConfig.java                # Redis cache with per-entity TTL
│   ├── WebSocketConfig.java            # STOMP broker configuration
│   ├── RabbitMQConfig.java             # Message queue setup
│   ├── StripeConfig.java               # Stripe API configuration
│   ├── EncryptionConfig.java           # AES-256 field-level encryption
│   ├── ResilienceConfig.java           # Circuit breaker + retry instances
│   ├── OpenApiConfig.java              # Swagger UI customization
│   ├── DatabaseConfig.java             # HikariCP connection pooling
│   ├── JacksonConfig.java              # JSON serialization
│   ├── WebMvcConfig.java               # CORS, interceptors
│   ├── EmailConfig.java                # SMTP mail sender
│   ├── LoggingConfig.java              # Structured logging
│   ├── OAuth2Config.java               # Google OAuth2 integration
│   ├── AdminAuditAspect.java           # Admin action auditing
│   ├── DataInitializer.java            # Seed data (categories, items, users)
│   ├── EnhancedDataInitializer.java    # Demo data seeding
│   └── SecretValidationConfig.java     # Secrets validation on startup
│
├── controller/                         # REST controllers (24 files)
│   ├── AuthController.java             # Register, login, refresh, password reset
│   ├── CategoryController.java         # Active categories
│   ├── MenuController.java             # Menu browsing, search
│   ├── CartController.java             # Cart CRUD
│   ├── OrderController.java            # Place order, history, cancel
│   ├── OrderModificationController.java# Modification requests
│   ├── OrderTrackingController.java    # Order tracking
│   ├── PaymentController.java          # Stripe payment intents
│   ├── StripeWebhookController.java    # Stripe webhook events
│   ├── CouponController.java           # Coupon validation
│   ├── ReviewController.java           # Customer reviews
│   ├── LoyaltyController.java          # Points, tiers, referrals
│   ├── InventoryController.java        # Stock management
│   ├── FileUploadController.java       # Image uploads
│   └── admin/                          # Admin controllers (11 files)
│       ├── AdminDashboardController.java
│       ├── AdminOrderController.java
│       ├── AdminMenuController.java
│       ├── AdminCategoryController.java
│       ├── AdminCouponController.java
│       ├── AdminPaymentController.java
│       ├── AdminUserController.java
│       ├── AdminReviewController.java
│       ├── AdminSettingsController.java
│       ├── AdminOperatingHoursController.java
│       └── AdminNotificationController.java
│
├── service/                            # Business logic (25 files)
│   ├── AuthService.java                # Authentication flows
│   ├── MenuItemService.java            # Menu item operations (cached)
│   ├── CategoryService.java            # Category CRUD
│   ├── OrderService.java               # Order lifecycle management
│   ├── OrderModificationService.java   # Modification approval workflow
│   ├── OrderProcessingConsumer.java    # Async order processing (RabbitMQ)
│   ├── CartService.java                # Cart operations
│   ├── PaymentService.java             # Stripe integration
│   ├── ResilientPaymentService.java    # Circuit-breaker protected payments
│   ├── CouponService.java              # Discount engine
│   ├── LoyaltyService.java             # Points, tiers, leaderboard
│   ├── InventoryService.java           # Stock tracking, low-stock alerts
│   ├── ReviewService.java              # Customer reviews
│   ├── EmailService.java               # Email sending
│   ├── AsyncEmailService.java          # Async email (RabbitMQ)
│   ├── EmailConsumer.java              # RabbitMQ email consumer
│   ├── NotificationService.java        # Push notifications
│   ├── NotificationConsumer.java       # WebSocket push consumer
│   ├── DashboardService.java           # KPI aggregation
│   ├── AdminUserService.java           # Admin user management
│   ├── AdminReviewService.java         # Review moderation
│   ├── SettingsService.java            # Restaurant configuration
│   ├── AuditService.java               # Audit trail logging
│   ├── FileStorageService.java         # File upload handling
│   └── FeatureFlagService.java         # Feature toggles (cached)
│
├── repository/                         # JPA repositories (26 files)
│   ├── UserRepository.java
│   ├── MenuItemRepository.java         # @EntityGraph for N+1 prevention
│   ├── CategoryRepository.java
│   ├── OrderRepository.java
│   ├── PaymentRepository.java
│   ├── CartRepository.java
│   ├── CartItemRepository.java
│   ├── OrderItemRepository.java
│   ├── OrderStatusLogRepository.java
│   ├── OrderModificationRepository.java
│   ├── ReviewRepository.java
│   ├── CouponRepository.java
│   ├── CouponUsageRepository.java
│   ├── LoyaltyProgramRepository.java
│   ├── UserLoyaltyRepository.java
│   ├── LoyaltyTransactionRepository.java
│   ├── InventoryRepository.java
│   ├── OperatingHoursRepository.java
│   ├── SettingsRepository.java
│   ├── RefreshTokenRepository.java
│   ├── PasswordResetTokenRepository.java
│   ├── MenuItemOptionGroupRepository.java
│   ├── AuditLogRepository.java
│   ├── IdempotencyKeyRepository.java
│   ├── FeatureFlagRepository.java
│   └── RefundRepository.java
│
├── entity/                             # JPA entities (28 files)
│   ├── User.java                       # Users with roles
│   ├── MenuItem.java                   # Menu items with dietary tags/allergens
│   ├── MenuItemOptionGroup.java        # Customization groups (size, extras)
│   ├── MenuItemOption.java             # Individual options
│   ├── Category.java                   # Menu categories
│   ├── Order.java                      # Orders with status lifecycle
│   ├── OrderItem.java                  # Order line items
│   ├── OrderStatusLog.java             # Status change audit
│   ├── OrderModification.java          # Modification requests
│   ├── Payment.java                    # Stripe payment records
│   ├── Refund.java                     # Refund records
│   ├── Cart.java / CartItem.java       # Shopping cart
│   ├── Coupon.java / CouponUsage.java  # Discounts
│   ├── Review.java                     # Customer reviews
│   ├── Inventory.java                  # Stock tracking
│   ├── LoyaltyProgram.java             # Program configuration
│   ├── UserLoyalty.java                # User points/tier
│   ├── LoyaltyTransaction.java         # Points history
│   ├── OperatingHours.java             # Weekly schedule
│   ├── Settings.java                   # Restaurant configuration
│   ├── AuditLog.java                   # Admin audit trail
│   ├── IdempotencyKey.java             # Idempotency for payments
│   ├── RefreshToken.java               # JWT refresh tokens
│   ├── PasswordResetToken.java         # Password reset tokens
│   ├── FeatureFlag.java                # Feature toggles
│   └── enums/                          # Enums (OrderStatus, UserRole, PaymentStatus, etc.)
│
├── dto/                                # Data transfer objects (64 files)
│   ├── auth/                           # LoginRequest, RegisterRequest, AuthResponse, etc.
│   ├── menu/                           # MenuItemRequest/Response, CategoryRequest/Response
│   ├── order/                          # PlaceOrderRequest, OrderResponse, etc.
│   ├── payment/                        # CreatePaymentIntentRequest, PaymentResponse, etc.
│   ├── cart/                           # AddToCartRequest, CartResponse, etc.
│   ├── coupon/                         # CouponValidationResponse
│   ├── admin/                          # AdminUserRequest/Response, DashboardStatsResponse, etc.
│   ├── inventory/                      # InventoryRequest/Response
│   ├── loyalty/                        # LoyaltyProgramRequest/Response, etc.
│   └── api/                            # Generic ApiResponse wrapper
│
├── security/                           # Security components
│   ├── JwtUtil.java                    # JWT generation and validation
│   ├── JwtAuthenticationFilter.java    # Request JWT filter
│   ├── CustomUserDetailsService.java   # User details loading
│   ├── CustomUserDetails.java          # Custom user principal
│   ├── AESUtil.java                    # Field-level encryption utility
│   ├── LoginRateLimitService.java      # Login attempt limiting
│   ├── RateLimitConfig.java            # Bucket4j configuration
│   └── RateLimitInterceptor.java       # Rate limiting interceptor
│
├── exception/                          # Exception handling
│   ├── GlobalExceptionHandler.java     # @ControllerAdvice global handler
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   ├── UnauthorizedException.java
│   ├── ConflictException.java
│   ├── PaymentException.java
│   ├── InventoryConflictException.java
│   ├── RateLimitExceededException.java
│   └── ServiceUnavailableException.java
│
├── aspect/                             # AOP aspects
│   ├── StructuredLoggingAspect.java    # Method-level logging with correlation IDs
│   └── IdempotencyInterceptor.java     # Idempotency key processing
│
├── annotation/                         # Custom annotations
│   ├── AuditLog.java                   # Admin action audit marker
│   └── Idempotent.java                 # Idempotency endpoint marker
│
├── converter/                          # JPA attribute converters
│   ├── AddressEncryptor.java           # AES-encrypted address storage
│   ├── CreditCardEncryptor.java        # AES-encrypted card storage
│   └── PhoneNumberEncryptor.java       # AES-encrypted phone storage
│
├── validator/                          # Custom validators
│   ├── ValidEmail.java / EmailValidator.java
│   ├── ValidPhoneNumber.java / PhoneNumberValidator.java
│   └── ValidCreditCard.java / CreditCardValidator.java
│
├── filter/                             # Servlet filters
│   └── CorrelationIdFilter.java        # Request correlation ID
│
├── health/                             # Health indicators
│   ├── DatabaseHealthIndicator.java
│   ├── RedisHealthIndicator.java
│   ├── RabbitMQHealthIndicator.java
│   ├── StripeHealthIndicator.java
│   ├── LivenessHealthIndicator.java
│   └── ReadinessHealthIndicator.java
│
├── metrics/                            # Custom metrics
│   └── BusinessMetrics.java            # Revenue, orders, customers metrics
│
└── task/                               # Scheduled tasks
    ├── OrderCancellationTask.java      # Auto-cancel pending orders
    ├── PointsExpiryTask.java           # Loyalty points expiry
    └── IdempotencyCleanupTask.java     # Cleanup old idempotency keys
```

---

## Features

### Core Business Features

**Order Management** — Full order lifecycle: creation → confirmation → preparation → ready → delivered/cancelled. Real-time status updates via WebSocket. Modification requests with admin approval workflow. Automatic cancellation of pending orders after configurable timeout.

**Menu Management** — Dynamic menu with categories, items, option groups (e.g., size, extras), dietary tags (vegetarian, vegan, gluten-free), and allergen tracking. Search and filter by category/name.

**Cart System** — Persistent shopping cart per user. Add/remove items with option selections. Special instructions per item. Quantity management.

**Payment Integration** — Stripe payment intents with card payments. Webhook handling for payment confirmation. Full refund processing. Multiple payment method support.

**Coupon Engine** — Percentage, fixed amount, and BOGO discount types. Usage limits (total and per-user). Minimum order amount and maximum discount caps. Date-range validity.

**Customer Reviews** — Rating and comment system. Admin moderation (approve/reject). Per-item review display.

**Loyalty Program** — Points-based rewards with tier progression (Bronze → Silver → Gold → Platinum). Referral bonuses. Points earning (10 per $1) and redemption (100 points = $1). Leaderboard.

**Inventory Tracking** — Real-time stock levels per menu item. Low-stock alerts. Restock history with supplier tracking.

### Technical Features

**Caching** — Redis multi-tier caching with configurable TTL per entity type (menu items: 2h, inventory: 5min, etc.).

**Async Processing** — RabbitMQ message queue for email sending, notifications, and order processing. Decouples heavy operations from HTTP request lifecycle.

**Resilience** — Resilience4j circuit breakers for Stripe API, email sending, notifications, and database. Retry with exponential backoff. Time limiters per operation type.

**Rate Limiting** — Bucket4j token-bucket algorithm. Per-role limits (admin: 1000/min, staff: 500/min, customer: 100/min).

**Security** — JWT access tokens (15min) + refresh tokens. OAuth2 (Google) social login. BCrypt password hashing. AES-256 field-level encryption for PII (addresses, credit cards, phone numbers). Role-based authorization (ADMIN, STAFF, CUSTOMER). Idempotency keys for payment/order operations.

**Observability** — Prometheus metrics (JVM, HTTP, cache, DB pool, custom business metrics). Health indicators for all dependencies. Structured logging with correlation IDs. Audit logging of admin actions.

---

## Prerequisites

- **Java 21+** (OpenJDK recommended)
- **Maven 3.9+** (wrapper included as `./mvnw`)
- **PostgreSQL 15+** (or use dev profile with H2 in-memory DB)
- **Redis 7+** (optional with dev profile)
- **RabbitMQ** (optional with dev profile)
- **Stripe account** (test mode for development)
- **Docker** (optional, for containerized environment)

---

## Quick Start

### Option 1: Dev Profile (H2, no Redis/RabbitMQ)

```bash
./mvnw clean package -DskipTests
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

The app starts at `http://localhost:8080` with:
- H2 in-memory database (console at `/h2-console`)
- Redis/RabbitMQ disabled
- Caching disabled
- Rate limiting disabled
- Pre-seeded demo data (categories, menu items, admin user)

### Option 2: Full Stack with Docker

```bash
docker-compose up -d
```

Starts PostgreSQL, Redis, RabbitMQ, and the application.

### Option 3: Manual Full Setup

```bash
# 1. Start dependencies
docker-compose up -d postgres redis rabbitmq

# 2. Build and run
./mvnw clean package -DskipTests
java -jar target/superMalle-*.jar
```

---

## Configuration

### Profiles

| Profile | Database | Redis | RabbitMQ | Use Case |
|---------|----------|-------|----------|----------|
| `default` | PostgreSQL | Required | Required | Production/staging |
| `dev` | H2 (in-memory) | Disabled | Disabled | Local development |

### Environment Variables

#### Database
| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | supermalle | Database username |
| `DB_PASSWORD` | supermalle123 | Database password |

#### Redis
| Variable | Default | Description |
|----------|---------|-------------|
| `REDIS_HOST` | localhost | Redis host |
| `REDIS_PORT` | 6379 | Redis port |
| `REDIS_PASSWORD` | (empty) | Redis password |

#### RabbitMQ
| Variable | Default | Description |
|----------|---------|-------------|
| `RABBITMQ_ENABLED` | true | Enable/disable message queue |
| `RABBITMQ_HOST` | localhost | RabbitMQ host |
| `RABBITMQ_PORT` | 5672 | RabbitMQ port |

#### JWT
| Variable | Default | Description |
|----------|---------|-------------|
| `JWT_SECRET` | (256-bit default) | JWT signing secret |
| `JWT_EXPIRATION` | 900000 | Access token expiry (ms) |

#### Stripe
| Variable | Default | Description |
|----------|---------|-------------|
| `STRIPE_API_KEY` | sk_test_... | Stripe secret key |
| `STRIPE_WEBHOOK_SECRET` | whsec_... | Webhook signing secret |

#### Email (SMTP)
| Variable | Default | Description |
|----------|---------|-------------|
| `EMAIL_HOST` | smtp.gmail.com | SMTP server |
| `EMAIL_PORT` | 587 | SMTP port |
| `EMAIL_USERNAME` | - | SMTP username |
| `EMAIL_PASSWORD` | - | SMTP password |

#### Application
| Variable | Default | Description |
|----------|---------|-------------|
| `FRONTEND_URL` | http://localhost:5173 | CORS allowed origin |
| `ADMIN_EMAIL` | admin@supermalle.com | Default admin email |
| `ADMIN_PASSWORD` | Admin@2026! | Default admin password |
| `ENCRYPTION_KEY` | (auto-generated) | AES encryption key (Base64) |
| `RATE_LIMIT_ENABLED` | true | Enable rate limiting |

---

## Database

### Schema

The database contains 28 tables covering:
- **Users & Auth**: `users`, `refresh_tokens`, `password_reset_tokens`
- **Menu**: `categories`, `menu_items`, `menu_item_option_groups`, `menu_item_options`
- **Orders**: `orders`, `order_items`, `order_status_logs`, `order_modifications`
- **Cart**: `carts`, `cart_items`
- **Payments**: `payments`, `refunds`
- **Coupons**: `coupons`, `coupon_usages`
- **Loyalty**: `loyalty_programs`, `user_loyalties`, `loyalty_transactions`
- **Inventory**: `inventories`
- **Reviews**: `reviews`
- **Operations**: `operating_hours`, `settings`
- **Infrastructure**: `audit_logs`, `idempotency_keys`, `feature_flags`

### Migrations

Flyway manages schema changes in `src/main/resources/db/migration/`.

### N+1 Query Prevention

All repository queries use `@EntityGraph` to eagerly fetch relationships and prevent `LazyInitializationException`:
- `MenuItemRepository`: all queries fetch `category` via `@EntityGraph`
- `CategoryRepository`: all queries fetch `menuItems` via `@EntityGraph`

---

## Caching Strategy

Redis-based caching with `@Cacheable` / `@CacheEvict` annotations.

| Cache Name | TTL | Eviction Triggers |
|------------|-----|-------------------|
| `menuItems` | 2 hours | Menu item create/update/delete |
| `categories` | 2 hours | Category create/update/delete |
| `loyaltyProgram` | 1 hour | Program update |
| `userLoyalty` | 10 minutes | Points earned/redeemed |
| `inventory` | 5 minutes | Stock update/restock |
| `coupons` | 30 minutes | Coupon create/update/delete |
| `settings` | 2 hours | Settings update |

Caching is disabled when using the `dev` profile.

---

## Security

### Authentication Flow
1. User logs in with email/password or Google OAuth2
2. Server validates credentials and returns JWT access token + refresh token
3. Client sends JWT in `Authorization: Bearer <token>` header
4. `JwtAuthenticationFilter` validates token on every request
5. Refresh tokens allow seamless token renewal

### Authorization
- **ADMIN**: Full access to all endpoints including admin panel
- **STAFF**: Order management, inventory, menu viewing
- **CUSTOMER**: Own orders, cart, profile, reviews

### Data Protection
- Passwords hashed with BCrypt
- PII fields (address, credit card, phone) encrypted with AES-256 at rest
- Idempotency keys prevent duplicate payment/order processing
- Rate limiting protects against brute-force and DoS attacks

### Additional Measures
- CORS restricted to configured origins
- Input validation on all endpoints (Jakarta Validation + custom validators)
- Structured audit log for all admin mutations
- Correlation IDs on every request for traceability

---

## Monitoring

### Health Checks
```
GET /actuator/health             → Overall status
GET /actuator/health/db          → Database connectivity
GET /actuator/health/redis       → Redis connectivity
GET /actuator/health/rabbitmq    → RabbitMQ connectivity
GET /actuator/health/stripe      → Stripe API health
GET /actuator/health/liveness    → Liveness probe
GET /actuator/health/readiness   → Readiness probe
```

### Metrics (Prometheus)
```
GET /actuator/prometheus
```

Custom business metrics: total revenue, order count, customer count, active users.

Prometheus config: `docker/prometheus/prometheus.yml`

### Logging
- Structured format with timestamp, level, correlation ID, message
- Separate error log file (`logs/supermalle-error.log`)
- Logback configuration: `logback-spring.xml`

---

## Docker

### Services

| Service | Image | Port |
|---------|-------|------|
| `app` | Custom build | 8080 |
| `postgres` | postgres:15 | 5432 |
| `redis` | redis:7 | 6379 |
| `rabbitmq` | rabbitmq:3-management | 5672, 15672 |
| `prometheus` | prom/prometheus | 9090 |

### Commands

```bash
# Development
docker-compose up -d

# Production
docker-compose -f docker-compose.prod.yml up -d

# Build image only
docker build -t supermalle:latest .

# Run with custom env
docker run -p 8080:8080 --env-file .env supermalle:latest
```

### CI/CD

GitHub Actions pipeline (`.github/workflows/ci-cd.yml`):
1. Checkout → JDK 21 setup
2. Build with Maven
3. Run tests
4. Build Docker image
5. Push to container registry
6. Deploy to staging/production

---

## Testing

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuthServiceTest

# Run integration tests
./mvnw verify
```

### Test Files

| Test | What it covers |
|------|---------------|
| `AuthServiceTest` | Registration, login, password reset flows |
| `OrderServiceTest` | Order creation, status transitions, cancellation |
| `PaymentServiceTest` | Stripe payment intent creation, webhook handling |
| `CouponEntityTest` | Discount calculations, validation logic |
| `IdempotencyInterceptorTest` | Idempotency key processing |
| `SuperMalleApplicationTests` | Context loading |

### QA Suite

Python-based admin control tests in `qa-tests/AdminControlTestSuite.py`.

---

## API Reference

For complete API documentation with request/response examples for all endpoints, see [API_REFERENCE.md](API_REFERENCE.md).

Swagger UI: `http://localhost:8080/swagger-ui.html`

### Quick Endpoint Overview

| Group | Endpoints |
|-------|-----------|
| **Auth** | POST register, login, refresh, change-password, forgot-password, reset-password |
| **Menu** | GET categories, menu, menu/{id}, menu/search, menu/category/{id} |
| **Cart** | GET cart, POST add, PUT update, DELETE remove, DELETE clear |
| **Orders** | POST place, GET list, GET {id}, POST {id}/cancel |
| **Payments** | POST create-intent, confirm, webhook, GET {id}, POST {id}/refund |
| **Coupons** | POST validate |
| **Reviews** | GET item/{id}, POST create |
| **Loyalty** | GET program, leaderboard, me, transactions, POST redeem, enroll, apply-referral |
| **Inventory** | GET list, GET {id}, PUT {id}, POST {id}/restock, GET low-stock |
| **Admin** | Dashboard, orders CRUD, menu CRUD, categories CRUD, coupons CRUD, payments, users CRUD, reviews moderation, settings, hours, announcements |

### Test Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@supermalle.com | Admin@2026! |
| Customer | test@example.com | test123 |
