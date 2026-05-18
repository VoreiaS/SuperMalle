# SuperMalle Restaurant Management System

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-green.svg)
![React](https://img.shields.io/badge/React-19-61DAFB.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

**A full-stack, production-ready restaurant management platform with order management, inventory tracking, loyalty programs, Stripe payments, and real-time operations.**

</div>

---

## Table of Contents

- [Overview](#overview)
- [System Architecture](#system-architecture)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Frontend Routes](#frontend-routes)
- [Database Schema](#database-schema)
- [Caching Strategy](#caching-strategy)
- [Security](#security)
- [Monitoring & Observability](#monitoring--observability)
- [Deployment](#deployment)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

SuperMalle is a comprehensive restaurant management system built for modern food service operations. The platform provides end-to-end functionality including:

- **Customer-facing**: Menu browsing, shopping cart, checkout with Stripe, order tracking, loyalty rewards
- **Admin-facing**: Dashboard with KPIs, order management, menu/category CRUD, coupon engine, payment reconciliation, review moderation, operating hours, user management, announcements
- **Real-time**: WebSocket-powered live order status updates and notifications
- **Resilient**: Circuit breakers (Resilience4j), rate limiting (Bucket4j), message queuing (RabbitMQ), Redis caching

The project was developed with a focus on production readiness, error resilience, and developer experience.

---

## System Architecture

```
┌────────────────────────────────────────────────────────────┐
│                    Client Layer                              │
│  React 19 + Vite + Tailwind CSS 4 + Zustand 5              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ Customer │  │  Admin   │  │ Checkout │  │   Auth   │   │
│  │   Pages  │  │  Pages   │  │  Pages   │  │  Pages   │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└────────────────────────────────────────────────────────────┘
         │              WebSocket              │
         ▼                                      ▼
┌────────────────────────────────────────────────────────────┐
│               Application Layer (REST API)                  │
│  Spring Boot 4.0.5 + Spring Security + Spring Data JPA     │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐   │
│  │ Orders │ │  Menu  │ │Payment │ │Inventory│ │Loyalty │   │
│  │Service │ │Service │ │Service │ │Service  │ │Service │   │
│  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘   │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐   │
│  │ Auth   │ │  Cart  │ │Coupon  │ │  User  │ │ Email  │   │
│  │Service │ │Service │ │Service │ │Service │ │Service │   │
│  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘   │
└────────────────────────────────────────────────────────────┘
         │              │              │
         ▼              ▼              ▼
┌──────────┐  ┌──────────────┐  ┌──────────────┐
│PostgreSQL│  │Redis (Cache) │  │  RabbitMQ    │
│ (JPA)    │  │  + Session   │  │  (Async)     │
└──────────┘  └──────────────┘  └──────────────┘
```

---

## Features

### Customer Features

| Feature | Description |
|---------|-------------|
| **Menu Browsing** | Browse/search menu items by category with dietary tags and allergens |
| **Product Detail** | View item details, option groups, customizations |
| **Shopping Cart** | Add/remove items, update quantities, persistent cart |
| **Checkout** | Delivery/pickup selection, Stripe card payment |
| **Order Tracking** | Real-time order status updates via WebSocket |
| **Order History** | View past orders and order details |
| **User Authentication** | JWT login, registration, OAuth2 (Google), password reset |
| **Profile Management** | Update profile, change password |
| **Loyalty Program** | Points accumulation, tier progression (Bronze/Silver/Gold/Platinum), leaderboard |

### Admin Features

| Feature | Description |
|---------|-------------|
| **Dashboard** | KPIs (revenue, orders, customers), sales charts, recent orders |
| **Order Management** | View/update/cancel orders, modify items, update ETA |
| **Menu Management** | CRUD for menu items with option groups, images, availability |
| **Category Management** | Organize items by category with sort order |
| **Coupon Engine** | Create/manage discounts (percentage, fixed, BOGO) with usage limits |
| **Payment Reconciliation** | View all payments, process refunds, filter by status/date |
| **User Management** | View/create/activate/deactivate users, password reset |
| **Review Moderation** | Approve/reject customer reviews |
| **Operating Hours** | Configure weekly hours, special closures |
| **Settings** | Restaurant config (tax rates, delivery charge, contact info) |
| **Announcements** | Send broadcast notifications to customers |

### Technical Features

| Category | Details |
|----------|---------|
| **Real-time** | WebSocket (STOMP) for live order/notification updates |
| **Caching** | Redis multi-tier caching with configurable TTL per entity |
| **Async Processing** | RabbitMQ for email, notification, and order processing |
| **Resilience** | Circuit breakers, retries, time limiters (Resilience4j) |
| **Rate Limiting** | Per-role rate limiting (Bucket4j) |
| **Idempotency** | Idempotency keys for payment and order creation |
| **Security** | JWT auth, OAuth2, role-based access, CORS, CSRF, field-level encryption |
| **Audit Logging** | Track admin actions with correlation IDs |
| **Observability** | Prometheus metrics, health indicators, structured logging |
| **Validation** | Server-side validation (Jakarta Validation), credit card/email/phone validators |

---

## Tech Stack

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Core language |
| Spring Boot | 4.0.5 | Application framework |
| Spring Security | 6.x | Authentication & authorization |
| Spring Data JPA | 3.x | Database ORM |
| Spring WebSocket | - | Real-time messaging |
| PostgreSQL | 15 | Primary database |
| H2 | - | Dev/test database |
| Redis | 7 | Caching |
| RabbitMQ | - | Async message queue |
| Stripe Java | 28.4.0 | Payment processing |
| JWT (jjwt) | 0.12.6 | Token auth |
| Resilience4j | 2.2.0 | Circuit breaker, retry |
| Bucket4j | 8.10.1 | Rate limiting |
| SpringDoc OpenAPI | 2.8.6 | API docs (Swagger) |
| Lombok | - | Boilerplate reduction |
| Micrometer + Prometheus | - | Metrics |
| Flyway | - | DB migrations |

### Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| React | 19.2 | UI framework |
| Vite | 8.0 | Build tool |
| Tailwind CSS | 4.2 | Styling |
| Zustand | 5.0 | State management |
| React Router DOM | 7.14 | Routing |
| Axios | 1.15 | HTTP client |
| Stripe.js | 9.4 | Payment UI |
| Lucide React | 1.8 | Icons |
| Recharts | 3.8 | Charts |
| Vitest | 4.1 | Testing |

---

## Project Structure

```
supermallfinal/
├── README.md                          # Project overview
├── .gitignore                         # Git exclusion rules
│
├── backend/                           # Spring Boot backend
│   ├── pom.xml                        # Maven build config
│   ├── mvnw / mvnw.cmd                # Maven wrapper
│   ├── Dockerfile                     # Container build
│   ├── docker-compose.yml             # Dev environment
│   ├── docker-compose.prod.yml        # Production environment
│   │
│   ├── src/main/java/com/example/superMalle/
│   │   ├── SuperMalleApplication.java          # Entry point
│   │   ├── config/                              # Spring configs
│   │   │   ├── SecurityConfig.java              # JWT + OAuth2 + CORS
│   │   │   ├── CacheConfig.java                 # Redis caching
│   │   │   ├── WebSocketConfig.java             # STOMP/WS
│   │   │   ├── RabbitMQConfig.java              # Message queue
│   │   │   ├── StripeConfig.java                # Payment gateway
│   │   │   ├── EncryptionConfig.java            # Field-level crypto
│   │   │   ├── ResilienceConfig.java            # Circuit breakers
│   │   │   ├── OpenApiConfig.java               # Swagger UI
│   │   │   ├── DataInitializer.java             # Seed data
│   │   │   └── EnhancedDataInitializer.java     # Demo seeding
│   │   ├── controller/                          # REST endpoints
│   │   │   ├── AuthController.java              # Login/register/refresh
│   │   │   ├── MenuController.java              # Menu browsing
│   │   │   ├── OrderController.java             # Order CRUD
│   │   │   ├── CartController.java              # Cart operations
│   │   │   ├── PaymentController.java           # Stripe payments
│   │   │   ├── StripeWebhookController.java     # Webhook handler
│   │   │   ├── CouponController.java            # Coupon validation
│   │   │   ├── ReviewController.java            # Customer reviews
│   │   │   ├── LoyaltyController.java           # Points & tiers
│   │   │   ├── InventoryController.java         # Stock management
│   │   │   ├── OrderTrackingController.java     # Tracking
│   │   │   ├── OrderModificationController.java # Order changes
│   │   │   └── admin/                           # Admin controllers
│   │   │       ├── AdminDashboardController.java
│   │   │       ├── AdminOrderController.java
│   │   │       ├── AdminMenuController.java
│   │   │       ├── AdminCategoryController.java
│   │   │       ├── AdminCouponController.java
│   │   │       ├── AdminPaymentController.java
│   │   │       ├── AdminUserController.java
│   │   │       ├── AdminReviewController.java
│   │   │       ├── AdminSettingsController.java
│   │   │       ├── AdminOperatingHoursController.java
│   │   │       └── AdminNotificationController.java
│   │   ├── service/                              # Business logic
│   │   │   ├── AuthService.java                  # Authentication
│   │   │   ├── MenuItemService.java              # Menu operations
│   │   │   ├── OrderService.java                 # Order lifecycle
│   │   │   ├── CartService.java                  # Cart management
│   │   │   ├── PaymentService.java               # Stripe integration
│   │   │   ├── ResilientPaymentService.java      # Fallback payments
│   │   │   ├── CouponService.java                # Discount engine
│   │   │   ├── LoyaltyService.java               # Points & tiers
│   │   │   ├── InventoryService.java             # Stock logic
│   │   │   ├── CategoryService.java              # Categories
│   │   │   ├── EmailService.java                 # Email sending
│   │   │   ├── AsyncEmailService.java            # Async email
│   │   │   ├── EmailConsumer.java                # RabbitMQ consumer
│   │   │   ├── NotificationService.java          # Notifications
│   │   │   ├── NotificationConsumer.java         # WS push
│   │   │   ├── OrderProcessingConsumer.java      # Async order proc
│   │   │   ├── DashboardService.java             # KPI aggregation
│   │   │   ├── AdminUserService.java             # User admin
│   │   │   ├── AdminReviewService.java           # Review moderation
│   │   │   ├── SettingsService.java              # Restaurant config
│   │   │   ├── AuditService.java                 # Audit trail
│   │   │   ├── FileStorageService.java           # File uploads
│   │   │   └── FeatureFlagService.java           # Feature toggles
│   │   ├── repository/                           # JPA repositories
│   │   │   ├── UserRepository.java
│   │   │   ├── MenuItemRepository.java
│   │   │   ├── CategoryRepository.java
│   │   │   ├── OrderRepository.java
│   │   │   ├── PaymentRepository.java
│   │   │   └── ... (26 total repositories)
│   │   ├── entity/                                # JPA entities
│   │   │   ├── User.java
│   │   │   ├── MenuItem.java
│   │   │   ├── Category.java
│   │   │   ├── Order.java
│   │   │   ├── Payment.java
│   │   │   ├── Cart.java
│   │   │   ├── Inventory.java
│   │   │   ├── Coupon.java
│   │   │   ├── Review.java
│   │   │   ├── LoyaltyProgram.java
│   │   │   └── ... (28 total entities + enums)
│   │   ├── dto/                                   # DTOs (64 files)
│   │   ├── security/                              # Security components
│   │   │   ├── JwtUtil.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── CustomUserDetailsService.java
│   │   │   ├── RateLimitInterceptor.java
│   │   │   └── ...
│   │   ├── exception/                             # Exception handling
│   │   ├── aspect/                                # AOP aspects
│   │   ├── annotation/                            # Custom annotations
│   │   ├── converter/                             # JPA converters
│   │   ├── validator/                             # Custom validators
│   │   ├── filter/                                # Servlet filters
│   │   ├── health/                                # Health indicators
│   │   └── task/                                  # Scheduled tasks
│   │
│   ├── src/main/resources/
│   │   ├── application.yml                        # Main config
│   │   ├── application-dev.yml                    # Dev profile
│   │   ├── db/migration/                          # Flyway scripts
│   │   └── templates/emails/                      # Email templates
│   │
│   ├── docker/
│   │   ├── prometheus/prometheus.yml              # Metrics scraping
│   │   └── grafana/                               # Dashboards
│   ├── scripts/                                   # DB backup/restore
│   └── qa-tests/                                  # Python QA suite
│
└── frontend/                                      # React frontend
    └── UiSuperMalle/
        ├── package.json                           # Dependencies
        ├── vite.config.js                         # Vite config
        ├── eslint.config.js                       # ESLint
        ├── index.html                             # Entry HTML
        │
        └── src/
            ├── main.jsx                           # Entry point
            ├── App.jsx                            # Router + layout
            ├── index.css                          # Global styles
            │
            ├── api/                               # Backend API client
            │   ├── client.js                      # Axios instance
            │   ├── endpoints.js                   # API endpoint map
            │   └── helpers.js                     # Utility functions
            │
            ├── store/                             # Zustand state
            │   ├── authStore.js                   # Auth & user
            │   ├── cartStore.js                   # Shopping cart
            │   └── toastStore.js                  # Toasts
            │
            ├── hooks/
            │   └── useWebSocket.js                # WS connection
            │
            ├── lib/
            │   └── validation.js                  # Validation utils
            │
            ├── components/
            │   ├── common/                        # Reusable
            │   │   ├── ErrorBoundary.jsx
            │   │   ├── Loading.jsx
            │   │   ├── Form.jsx
            │   │   └── ToastContainer.jsx
            │   └── layout/                        # Layout
            │       ├── Navbar.jsx
            │       ├── Footer.jsx
            │       └── AdminSidebar.jsx
            │
            └── pages/
                ├── customer/                      # 13 customer pages
                │   ├── HomePage.jsx
                │   ├── MenuPage.jsx
                │   ├── MenuItemPage.jsx
                │   ├── CartPage.jsx
                │   ├── CheckoutPage.jsx
                │   ├── OrdersPage.jsx
                │   ├── OrderDetailPage.jsx
                │   ├── ProfilePage.jsx
                │   ├── LoginPage.jsx
                │   ├── RegisterPage.jsx
                │   ├── ForgotPasswordPage.jsx
                │   ├── ResetPasswordPage.jsx
                │   └── OAuth2CallbackPage.jsx
                ├── admin/                         # 11 admin pages
                │   ├── AdminDashboard.jsx
                │   ├── AdminOrders.jsx
                │   ├── AdminMenu.jsx
                │   ├── AdminCategories.jsx
                │   ├── AdminCoupons.jsx
                │   ├── AdminPayments.jsx
                │   ├── AdminUsers.jsx
                │   ├── AdminReviews.jsx
                │   ├── AdminSettings.jsx
                │   ├── AdminOperatingHours.jsx
                │   └── AdminAnnounce.jsx
                ├── checkout/
                │   └── StripeCheckoutPage.jsx
                ├── orders/
                │   ├── OrderConfirmationPage.jsx
                │   └── OrderTrackingPage.jsx
                └── error/
                    ├── NotFoundPage.jsx
                    └── ServerErrorPage.jsx
```

---

## Quick Start

### Prerequisites

- **Java 21+** and **Maven 3.9+** (backend)
- **Node.js 18+** and **npm** (frontend)
- **PostgreSQL 15+** (or use H2 for dev)
- **Redis 7+** (or disable with `dev` profile)
- **RabbitMQ** (or disable with `dev` profile)
- **Stripe account** for payment features

### Backend Setup

```bash
# 1. Clone and enter the project
git clone https://github.com/VoreiaS/SuperMalle.git
cd SuperMalle/backend

# 2. Build the application
./mvnw clean package -DskipTests

# 3. Run with dev profile (H2 in-memory DB, no Redis/RabbitMQ needed)
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run

# The API will be available at http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
# H2 Console: http://localhost:8080/h2-console
```

### Frontend Setup

```bash
# In a separate terminal
cd frontend/UiSuperMalle

# 1. Install dependencies
npm install

# 2. Copy and configure environment
cp .env.example .env
# Edit .env as needed

# 3. Start development server
npm run dev

# The app will be available at http://localhost:5173
```

### Docker Setup (Full Stack)

```bash
# Start all services (PostgreSQL, Redis, RabbitMQ, backend, frontend)
cd backend
docker-compose up -d
```

---

## Configuration

### Environment Variables

#### Backend (`backend/.env`)

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | supermalle | Database username |
| `DB_PASSWORD` | supermalle123 | Database password |
| `REDIS_HOST` | localhost | Redis host |
| `REDIS_PORT` | 6379 | Redis port |
| `RABBITMQ_ENABLED` | true | Enable message queue |
| `JWT_SECRET` | (256-bit key) | JWT signing secret |
| `JWT_EXPIRATION` | 900000 | Token expiry (ms) |
| `STRIPE_API_KEY` | sk_test_... | Stripe secret key |
| `STRIPE_WEBHOOK_SECRET` | whsec_... | Stripe webhook secret |
| `EMAIL_HOST` | smtp.gmail.com | SMTP server |
| `EMAIL_USERNAME` | - | SMTP username |
| `EMAIL_PASSWORD` | - | SMTP password |
| `FRONTEND_URL` | http://localhost:5173 | CORS origin |
| `ADMIN_EMAIL` | admin@supermalle.com | Default admin |
| `ADMIN_PASSWORD` | Admin@2026! | Default admin password |

#### Frontend (`frontend/UiSuperMalle/.env`)

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_URL` | http://localhost:8080/api/v1 | Backend API base |
| `VITE_WS_URL` | ws://localhost:8080/ws | WebSocket URL |
| `VITE_STRIPE_PUBLIC_KEY` | pk_test_... | Stripe publishable key |
| `VITE_GOOGLE_CLIENT_ID` | - | Google OAuth2 client ID |
| `VITE_ENABLE_ANALYTICS` | false | Enable analytics |
| `VITE_ENV` | development | Environment label |

### Profiles

- **default**: Full stack (PostgreSQL, Redis, RabbitMQ required)
- **dev**: H2 in-memory DB, Redis/RabbitMQ disabled, caching disabled, rate limiting disabled — ideal for local development

Run with dev profile:
```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

---

## API Reference

### Base URL

| Environment | URL |
|-------------|-----|
| Development | `http://localhost:8080/api/v1` |
| Production | `https://api.supermalle.com/api/v1` |

### Authentication

Most endpoints require a JWT token in the `Authorization` header:
```
Authorization: Bearer <your-jwt-token>
```

### Public Endpoints

#### Auth
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/auth/register` | Register new customer |
| POST | `/api/v1/auth/login` | Login (returns JWT) |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| POST | `/api/v1/auth/change-password` | Change password |
| POST | `/api/v1/auth/forgot-password` | Request password reset |
| POST | `/api/v1/auth/reset-password` | Reset with token |
| POST | `/api/v1/auth/logout` | Invalidate token |

#### Menu
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/categories` | Active categories with item count |
| GET | `/api/v1/menu` | Menu items grouped by category |
| GET | `/api/v1/menu/{id}` | Single menu item detail |
| GET | `/api/v1/menu/search?q=` | Search menu items |
| GET | `/api/v1/menu/category/{id}` | Items by category |

#### Cart
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/cart` | Get current cart |
| POST | `/api/v1/cart/add` | Add item to cart |
| PUT | `/api/v1/cart/update/{itemId}` | Update quantity |
| DELETE | `/api/v1/cart/remove/{itemId}` | Remove item |

#### Orders
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/orders` | Place new order |
| GET | `/api/v1/orders` | User's order history |
| GET | `/api/v1/orders/{id}` | Order detail |
| POST | `/api/v1/orders/{id}/cancel` | Cancel order |
| POST | `/api/v1/orders/{id}/modify` | Request modification |

#### Payments
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/payments/create-intent` | Create Stripe payment intent |
| POST | `/api/v1/payments/confirm` | Confirm payment |
| POST | `/api/v1/payments/webhook` | Stripe webhook handler |
| POST | `/api/v1/payments/{id}/refund` | Process refund |

#### Coupons
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/coupons/validate` | Validate coupon code |

#### Reviews
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/reviews/item/{itemId}` | Item reviews |
| POST | `/api/v1/reviews` | Submit review |

#### Loyalty
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/loyalty/program` | Program details |
| GET | `/api/v1/loyalty/me` | User loyalty status |
| GET | `/api/v1/loyalty/me/transactions` | Points history |
| POST | `/api/v1/loyalty/redeem` | Redeem points |
| GET | `/api/v1/loyalty/leaderboard` | Top members |

#### Inventory
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/inventory` | All stock |
| GET | `/api/v1/inventory/{id}` | Item stock |

### Admin Endpoints

All admin endpoints require `ADMIN` role.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/admin/dashboard` | Dashboard KPIs + charts |
| GET | `/api/v1/admin/orders` | All orders (paginated, filterable) |
| PUT | `/api/v1/admin/orders/{id}/status` | Update order status |
| GET | `/api/v1/admin/menu` | All menu items (paginated) |
| POST | `/api/v1/admin/menu` | Create menu item |
| PUT | `/api/v1/admin/menu/{id}` | Update menu item |
| DELETE | `/api/v1/admin/menu/{id}` | Delete menu item |
| GET | `/api/v1/admin/categories` | All categories |
| POST | `/api/v1/admin/categories` | Create category |
| PUT | `/api/v1/admin/categories/{id}` | Update category |
| DELETE | `/api/v1/admin/categories/{id}` | Soft-delete category |
| GET | `/api/v1/admin/coupons` | All coupons |
| POST | `/api/v1/admin/coupons` | Create coupon |
| PUT | `/api/v1/admin/coupons/{id}` | Update coupon |
| DELETE | `/api/v1/admin/coupons/{id}` | Delete coupon |
| GET | `/api/v1/admin/payments` | All payments (filterable) |
| POST | `/api/v1/admin/payments/{id}/refund` | Admin refund |
| GET | `/api/v1/admin/users` | All users |
| POST | `/api/v1/admin/users` | Create user (with generated password) |
| PUT | `/api/v1/admin/users/{id}` | Update user |
| POST | `/api/v1/admin/users/{id}/reset-password` | Force password reset |
| GET | `/api/v1/admin/reviews` | All reviews (pending/approved/rejected) |
| PUT | `/api/v1/admin/reviews/{id}/approve` | Approve review |
| PUT | `/api/v1/admin/reviews/{id}/reject` | Reject review |
| GET/PUT | `/api/v1/admin/hours` | Operating hours |
| GET/PUT | `/api/v1/admin/settings` | Restaurant settings |
| POST | `/api/v1/admin/announce` | Send announcement |

Full API documentation is available at `http://localhost:8080/swagger-ui.html` (Swagger UI).

---

## Frontend Routes

| Path | Page | Access |
|------|------|--------|
| `/` | HomePage | Public |
| `/menu` | MenuPage | Public |
| `/menu/:id` | MenuItemPage | Public |
| `/login` | LoginPage | Public |
| `/register` | RegisterPage | Public |
| `/forgot-password` | ForgotPasswordPage | Public |
| `/reset-password` | ResetPasswordPage | Public |
| `/oauth2/callback` | OAuth2CallbackPage | Public |
| `/cart` | CartPage | Authenticated |
| `/checkout` | CheckoutPage | Authenticated |
| `/checkout/stripe` | StripeCheckoutPage | Authenticated |
| `/checkout/pay/:orderId` | StripeCheckoutPage | Authenticated |
| `/orders` | OrdersPage | Authenticated |
| `/orders/:id` | OrderDetailPage | Authenticated |
| `/orders/:id/confirmation` | OrderConfirmationPage | Authenticated |
| `/orders/:id/track` | OrderTrackingPage | Authenticated |
| `/profile` | ProfilePage | Authenticated |
| `/admin` | AdminDashboard | Admin |
| `/admin/orders` | AdminOrders | Admin |
| `/admin/menu` | AdminMenu | Admin |
| `/admin/categories` | AdminCategories | Admin |
| `/admin/coupons` | AdminCoupons | Admin |
| `/admin/payments` | AdminPayments | Admin |
| `/admin/users` | AdminUsers | Admin |
| `/admin/reviews` | AdminReviews | Admin |
| `/admin/hours` | AdminOperatingHours | Admin |
| `/admin/settings` | AdminSettings | Admin |
| `/admin/announce` | AdminAnnounce | Admin |
| `/500` | ServerErrorPage | Public |
| `*` | NotFoundPage (404) | Public |

---

## Database Schema

### Entity Model

```
User ──── Cart ──── CartItem ──── MenuItem ──── Category
 │                                        │
 ├─── Order ──── OrderItem ───────────────┘
 │       │                               
 │       ├─── OrderStatusLog             MenuItemOptionGroup ──── MenuItemOption
 │       ├─── OrderModification         
 │       └─── Payment ──── Refund       MenuItem ──── Inventory
 │
 ├─── UserLoyalty ──── LoyaltyTransaction     Coupon ──── CouponUsage
 │       │
 │       └─── LoyaltyProgram              
 │
 ├─── Review                             AuditLog
 │
 ├─── PasswordResetToken
 │
 └─── RefreshToken
```

### Key Tables (28 entities)

| Entity | Purpose |
|--------|---------|
| `User` | Customers, staff, admins |
| `MenuItem` | Menu items with price, dietary tags, allergens |
| `Category` | Menu categories with sort order |
| `MenuItemOptionGroup` | Customization groups (size, extras) |
| `MenuItemOption` | Individual options within a group |
| `Cart` / `CartItem` | Shopping cart |
| `Order` / `OrderItem` | Orders with status lifecycle |
| `OrderStatusLog` | Status change audit trail |
| `OrderModification` | Customer modification requests |
| `Payment` / `Refund` | Stripe payment records |
| `Coupon` / `CouponUsage` | Discount management |
| `Inventory` | Stock tracking |
| `Review` | Customer reviews |
| `LoyaltyProgram` / `UserLoyalty` / `LoyaltyTransaction` | Rewards system |
| `OperatingHours` | Weekly schedule |
| `Settings` | Restaurant configuration |
| `AuditLog` | Admin action audit trail |
| `IdempotencyKey` | Idempotency for payments |
| `FeatureFlag` | Feature toggles |
| `PasswordResetToken` / `RefreshToken` | Auth tokens |

---

## Caching Strategy

| Cache Name | TTL | Data |
|------------|-----|------|
| `menuItems` | 2 hours | Menu item details |
| `categories` | 2 hours | Categories with counts |
| `loyaltyProgram` | 1 hour | Program configuration |
| `userLoyalty` | 10 minutes | User points/tier |
| `inventory` | 5 minutes | Stock levels |
| `coupons` | 30 minutes | Active coupons |
| `settings` | 2 hours | Restaurant settings |

---

## Security

### Authentication
- **JWT**: Access tokens with 15-minute expiry, refresh tokens supported
- **OAuth2**: Google Sign-In integration
- **Rate Limiting**: Bucket4j — 100 req/min default, 1000 for admins
- **Password**: BCrypt hashing, reset via email token

### Authorization
- **Roles**: `ADMIN`, `STAFF`, `CUSTOMER`
- **Protected routes**: Server-side `@PreAuthorize` + client-side route guards
- **CORS**: Configurable allowed origins

### Data Protection
- **Field-level encryption**: AES-256 for PII (addresses, credit cards, phone numbers)
- **Idempotency**: Payment/order creation protected by idempotency keys
- **Audit**: All admin mutations logged with correlation IDs
- **Validation**: Server-side input validation with custom validators (email, phone, credit card)

---

## Monitoring & Observability

### Health Checks
```
GET /actuator/health              → Overall status
GET /actuator/health/db           → Database connectivity
GET /actuator/health/redis        → Redis connectivity
GET /actuator/health/rabbitmq     → RabbitMQ connectivity
GET /actuator/health/stripe       → Stripe API health
```

### Metrics (Prometheus)
```
GET /actuator/prometheus
```

Metrics available: JVM memory, HTTP request rates, cache hit/miss, DB connection pool, custom business metrics.

### Logging
- Structured logging with correlation IDs
- Logback configuration in `logback-spring.xml`
- Separate error log file

### Alerting
Prometheus alert rules in `docker/prometheus/alert_rules.yml`

---

## Deployment

### Docker (Recommended)

```bash
# Development stack
cd backend
docker-compose up -d

# Production stack
docker-compose -f docker-compose.prod.yml up -d
```

### Manual

```bash
# Backend
cd backend
./mvnw clean package -DskipTests
java -jar target/superMalle-*.jar --spring.profiles.active=dev

# Frontend
cd frontend/UiSuperMalle
npm run build
# Serve the dist/ directory via nginx or your CDN
```

### CI/CD
GitHub Actions pipeline (`.github/workflows/ci-cd.yml`) with:
- Build & test
- Dependency scanning
- Docker build & push
- Deployment to staging/production

---

## Testing

### Backend
```bash
cd backend
./mvnw test                    # Unit tests
./mvnw verify                  # Integration tests
```

Test files:
- `AuthServiceTest.java` — Auth flows
- `OrderServiceTest.java` — Order lifecycle
- `PaymentServiceTest.java` — Stripe integration
- `CouponEntityTest.java` — Coupon logic
- `IdempotencyInterceptorTest.java` — Idempotency

### Frontend
```bash
cd frontend/UiSuperMalle
npm run test                   # Vitest
npm run test:coverage          # With coverage
npm run test:ui                # UI mode
```

### QA Suite
Python-based admin QA tests in `backend/qa-tests/AdminControlTestSuite.py`.

---

## Project Statistics

| Metric | Value |
|--------|-------|
| Backend Java files | ~210 |
| Backend lines of code | ~25,000 |
| Frontend source files | ~50 |
| Frontend lines of code | ~12,000 |
| API endpoints | 60+ |
| Database entities | 28 |
| Services | 25 |
| Controllers | 24 |
| Repositories | 26 |
| DTOs | 60+ |
| Dependencies (Maven) | 40+ |
| NPM packages | 30+ |
| Database tables | 28 |
| Docker services | 5 (app, db, redis, rabbitmq, prometheus) |

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes following [Conventional Commits](https://www.conventionalcommits.org/):
   - `feat:` new feature
   - `fix:` bug fix
   - `docs:` documentation
   - `refactor:` code refactoring
   - `test:` testing
   - `chore:` build/tooling
4. Push: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

## License

This project is licensed under the MIT License.

---

<div align="center">

**Built with ❤️ by the SuperMalle Team**

⭐ If you find this project useful, please give it a star on GitHub!

</div>
