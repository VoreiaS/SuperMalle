# UNIMPLEMENTED FEATURES — Full Analysis & Implementation Plan
## SuperMalle Restaurant System | May 2026

> **Methodology:** Cross-referenced every entity, service, controller, config file, API doc,
> QA audit report (103 findings), gap analysis, endpoint test results, frontend pages,
> and Java source imports to identify features that are **planned/referenced/configured
> but NOT actually implemented** or are **implemented as stubs**.

---

## TABLE OF CONTENTS

1. [EXECUTIVE SUMMARY](#1-executive-summary)
2. [TIER 1 — CRITICAL (Financial Loss / Legal Risk)](#2-tier-1--critical-financial-loss--legal-risk)
3. [TIER 2 — HIGH (Operational Failure)](#3-tier-2--high-operational-failure)
4. [TIER 3 — MEDIUM (Missing Core Features)](#4-tier-3--medium-missing-core-features)
5. [TIER 4 — LOW (Production Polish)](#5-tier-4--low-production-polish)
6. [PHASE-BASED ROADMAP](#6-phase-based-roadmap)
7. [REAL-WORLD REASONING APPENDIX](#7-real-world-reasoning-appendix)

---

## 1. EXECUTIVE SUMMARY

### By the Numbers

| Category | Count |
|----------|-------|
| **Stub/Mock implementations** (code that returns fake data) | 3 |
| **Missing entities** (exists in workflow but no DB table) | 6 |
| **Incomplete services** (logic partial or wrong) | 8 |
| **Unhandled API events** (webhooks, error types) | 5+ |
| **Missing controllers/endpoints** (frontend expects them) | 4+ |
| **No test coverage** on | 95%+ of codebase |
| **Config duplicated** (same values in different namespaces) | 2 sets |

### Architectural Weaknesses

1. **Single-restaurant hardcode** — No `Restaurant` entity; all data is global. Every real POS system (Toast, Square) is multi-location from day 1.
2. **No soft delete** — Hard deletes will cascade-corrupt orders, payments, reviews when a user is removed.
3. **No database migrations** — Hibernate `ddl-auto: update` is a dev convenience. Production needs Flyway/Liquibase.
4. **In-memory-only rate limiting** — Bucket4j in a `ConcurrentHashMap` resets on restart and doesn't work across instances.
5. **No receipt/invoice system** — Customers receive nothing after paying. Every restaurant system generates receipts.

---

## 2. TIER 1 — CRITICAL (Financial Loss / Legal Risk)

### 2.1 ResilientPaymentService is a complete stub

**Location:** `service/ResilientPaymentService.java:103-118`

**What's wrong:**
```java
private String createPaymentIntentInternal(BigDecimal amount, String currency, String customerId) {
    // In production, this would call Stripe API...
    return "pi_" + System.currentTimeMillis(); // MOCK
}
```
The entire class — circuit breaker, retry, time limiter — wraps a function that returns fake data. It's never actually called by `PaymentService`; the real Stripe calls happen in `PaymentService.createPaymentIntent()` without any resilience wrapping.

**Real-world impact:** In production, Stripe API calls have **no circuit breaker, no retry, no timeout**. A Stripe outage or network blip = failed payments = lost revenue + angry customers.

**Implementation plan:**
1. Integrate `ResilientPaymentService` into the actual payment flow by replacing direct Stripe calls in `PaymentService` with calls through `ResilientPaymentService`
2. Remove the mock and wire up real `PaymentIntent.create()` inside the resilient wrapper
3. Configure proper timeouts: Stripe API should timeout at 10s (not default infinity)
4. Add fallback: if circuit breaker is open, queue the payment for retry via RabbitMQ

**Expected outcome:** Payment operations are resilient to transient Stripe API failures with circuit breaker (50% failure threshold), retry (3 attempts, exponential backoff), and timeout (10s). Zero failed payments due to network blips.

---

### 2.2 Order total calculation incomplete — no tip, per-item tax, or price breakdown stored

**Location:** `service/OrderService.java:123-172`, `entity/Order.java`

**What's wrong:**
- `subtotalAmount` and `taxAmount` are now on the `Order` entity but `tipAmount` is not included in the total calculation formula
- Tax uses a **single flat rate** (`app.restaurant.tax-rate`) for ALL items — alcohol is taxed differently than food in most jurisdictions
- `scale=2` on monetary fields causes rounding errors with fractional tax rates (e.g., 8.625% NYC sales tax)
- `totalAmount = subtotal + tax + deliveryFee - discount` — missing `+ tipAmount`

**Real-world impact:**
- Tips lost = revenue leakage
- Incorrect tax calculation = audit risk, legal penalties
- No per-item tax = can't itemize on receipts

**Implementation plan:**
1. Fix `toResponse()` missing `tipAmount` in OrderResponse builder
2. Add `taxCategory` to `MenuItem` entity (enum: `STANDARD`, `ALCOHOL`, `PREPARED_FOOD`, `GROCERY`)
3. Change monetary precision from `(10, 2)` to `(12, 4)` for internal calculation, round to `(10, 2)` only at display
4. Create `TaxService` that calculates per-item tax based on `taxCategory` + restaurant location
5. Update `placeOrder()` to include `tipAmount` from `PlaceOrderRequest`
6. Add `scheduledFor` field to `PlaceOrderRequest` for pre-orders

**Expected outcome:** Order totals are legally compliant with per-item tax breakdown, tips are tracked as separate line item, and monetary calculations are accurate at scale=4 with HALF_UP rounding.

---

### 2.3 No restaurant operating hours validation

**Location:** `service/OrderService.java:54` — `placeOrder()` has NO check

**What's wrong:** Orders can be placed at 3 AM when the restaurant is closed. The `OperatingHours` entity doesn't exist.

**Real-world impact:** Customer places order at 2 AM, restaurant is closed, customer never gets food, chargeback, bad reviews, brand damage.

**Implementation plan:**
1. Create `OperatingHours` entity: `id`, `restaurantId`, `dayOfWeek` (MON-SUN), `openTime` (LocalTime), `closeTime` (LocalTime), `isClosed` (boolean)
2. Create `OperatingHoursRepository`
3. Create `OperatingHoursService` with methods: `isOpenNow()`, `isOpenAt(LocalDateTime)`, `getNextOpenTime()`
4. Add check in `OrderService.placeOrder()`:
   - If `orderType == PICKUP` or `DELIVERY` and no `scheduledFor` → validate restaurant is open NOW
   - If `scheduledFor` is set → validate restaurant is open at that time
5. Add `OperatingHours` CRUD to admin panel
6. Return clear error: `"Restaurant is currently closed. Opens at {openTime}"`

**Expected outcome:** Zero orders placed outside business hours. Customers get clear message about operating hours instead of a failed order.

---

### 2.4 No soft delete on any entity

**Location:** ALL entities — no `deletedAt`, `@SoftDelete`, or `@Where` clause

**What's wrong:** Hard-deleting a `User` cascades to `Order` → `Payment` → `Refund`. `MenuItem` soft-delete is done manually via `isActive`/`isAvailable` but inconsistently. Newer entities like `Inventory` have `isActive`. No unified approach.

**Real-world impact:**
- Deleting a user who placed orders 6 months ago = orphaned orders = accounting nightmare
- Regulatory requirement: financial records must be retained for 7 years (GDPR, PCI-DSS)

**Implementation plan:**
1. Add `@SoftDelete` (Hibernate 6.4+) or add `deletedAt` + `@Where(clause="deleted_at IS NULL")` to: `User`, `MenuItem`, `Category`, `Coupon`, `Order`, `Payment`
2. Create `@MappedSuperclass` base entity `SoftDeletableEntity` with `deletedAt`, `deletedBy`
3. Create repository-level filter using Spring Data `@Query` with `AND deleted_at IS NULL`
4. Add `admin/restore` endpoints for soft-deleted entities
5. For entities where hard delete is OK (Cart, CartItem, OrderStatusLog), leave as-is

**Expected outcome:** Zero data loss risk. All financial/regulatory records are retained even after "deletion". Admin can restore accidentally deleted records.

---

### 2.5 Stripe webhook missing critical event handlers

**Location:** `controller/StripeWebhookController.java:82-111`

**What's wrong:** Only 4 events are handled. Missing:
- `charge.dispute.created` — chargebacks (financial loss if ignored)
- `payment_intent.canceled` — payment cancellation
- `charge.refund.updated` — refund status changes
- `charge.captured` — capture confirmation

Also: `charge.refunded` handler is a stub (`// informational` — just logs)

**Real-world impact:**
- A chargeback goes undetected → money is withdrawn from bank account with no record → accounting chaos
- Payment cancellations not tracked → inventory is depleted for orders that won't be paid

**Implementation plan:**
1. Add `handleChargeDisputeCreated()` → set `PaymentStatus.DISPUTED`, notify admin, freeze order fulfillment
2. Add `handlePaymentIntentCanceled()` → set `PaymentStatus.CANCELLED`, release inventory hold
3. Add `handleChargeRefundUpdated()` → update `Refund.status` in our DB
4. Add `RefundStatus` enum: `PENDING`, `PROCESSING`, `SUCCEEDED`, `FAILED`, `CANCELLED`
5. Add `PaymentStatus.DISPUTED` to enum
6. Return `500` for deserialization failures instead of `400` so Stripe retries
7. Never return `200` for exceptions — Stripe interprets `200` as "processed successfully" and won't retry

**Expected outcome:** Full lifecycle payment tracking. Chargebacks are immediately flagged. Zero silent payment failures. Stripe webhooks are properly idempotent.

---

### 2.6 No email/SMS notification delivery

**Location:** `AsyncEmailService.java`, `AsyncNotificationService.java`, `EmailConfig.java`

**What's wrong:**
- `AsyncEmailService` and `AsyncNotificationService` are wired to RabbitMQ queues with full infrastructure (DLQs, exchanges, routing keys) but **no actual sending logic exists**
- `EmailConfig` has JavaMailSender configured but it's never invoked from business flows
- The RabbitMQ queues are created and consumers exist, but the actual SMTP/email sending is commented out/stub
- `ResilientPaymentService.sendEmailInternal()` is also a complete stub (returns `null`)

**Real-world impact:**
- Customers receive NO order confirmation email after paying
- No "Your order is ready" email
- No password reset emails
- No promotional emails
- WebSocket-only notifications — user must be on the website to see anything

**Implementation plan:**
1. **Phase A** — Wire email into order lifecycle:
   - `OrderService.placeOrder()` → publish to `email.queue` with order confirmation payload
   - `OrderService.updateOrderStatus()` → publish status update email
   - Create `Thymeleaf` email templates for: order confirmation, order status update, password reset, welcome email
2. **Phase B** — Implement `AsyncEmailService.sendEmail()` with real JavaMailSender:
   - Build MimeMessage with HTML template
   - Handle send failures → DLQ → retry (max 3)
   - Track delivery status
3. **Phase C** — Add SMS notifications (Twilio or similar):
   - `AsyncNotificationService` text messages for critical updates: "Your order is out for delivery!"
4. **Phase D** — Create `Notification` entity for persistent notification history:
   - Store all notifications in DB
   - Add `/api/v1/notifications` endpoint for users to view history

**Expected outcome:** Every order trigger fires at least one email notification. Zero "I never received my order" support tickets. Notifications are persistent and queryable.

---

## 3. TIER 2 — HIGH (Operational Failure)

### 3.1 No Kitchen Display System (KDS)

**Location:** Missing entirely — referenced only via `notificationService.notifyKitchenNewOrder()`

**What's wrong:** Kitchen gets a WebSocket notification that "a new order arrived" but has no way to:
- View all pending orders in priority order
- Mark items as "started cooking"
- Mark items as "completed"
- See preparation times and SLA status
- Bump (remove) completed orders

**Real-world impact:** In a real restaurant, the kitchen display is the SECOND most critical system after POS. Without it, kitchen staff uses paper tickets = lost orders, wrong items, long wait times.

**Implementation plan:**
1. Add `/api/v1/kitchen` controller with endpoints:
   - `GET /orders` — queue of confirmed/preparing orders sorted by time
   - `POST /orders/{id}/start-preparing` — mark order as PREPARING
   - `POST /orders/{orderId}/items/{itemId}/start` — per-item tracking
   - `POST /orders/{id}/mark-ready` — mark order as READY
2. Add KDS-specific WebSocket topic `/topic/kitchen`
3. Add SLA tracking: show time elapsed per order item, highlight items approaching target time
4. Create KDS frontend page or document the API for third-party KDS integration

**Expected outcome:** Kitchen has a real-time digital order queue. Order items are tracked at individual level. SLA violations are visible in real-time.

---

### 3.2 No scheduled pre-orders / order scheduling

**Location:** `entity/Order.java` — no `scheduledFor` field. `OrderService.placeOrder()` — no check.

**What's wrong:** All orders are immediate. Customers cannot place an order for a future time (lunch pre-order, office catering, etc.). `PlaceOrderRequest` has no `scheduledFor` field.

**Real-world impact:** Lost revenue from catering, office lunches, and scheduled pickups. Competitors (Toast, Square Online) all support pre-ordering.

**Implementation plan:**
1. Add `scheduledFor` (LocalDateTime) to `Order` entity and `PlaceOrderRequest` DTO
2. Add `SCHEDULED` state to `OrderStatus` enum
3. Create scheduled task (`@Scheduled`) that promotes `SCHEDULED` orders to `PENDING` when `scheduledFor` arrives
4. Validate `scheduledFor` against operating hours and minimum preparation window
5. Add admin endpoint to view/manage scheduled orders

**Expected outcome:** Customers can pre-order for any future time slot. Orders automatically activate when the scheduled time arrives. New revenue stream from catering and office lunch orders.

---

### 3.3 No delivery driver dispatch system

**Location:** `entity/Order.java` — no `driver` (User) field. No `DELIVERY` role user management.

**What's wrong:** Delivery orders have no driver assigned. The system supports `DELIVERY` as an `OrderType` and `DELIVERY` as a `UserRole`, but there's:
- No driver assignment flow
- No driver mobile app API
- No driver status tracking
- No delivery zone management

**Real-world impact:** Every delivery order is unassignable. A restaurant would need a third-party dispatch system or manual phone calls.

**Implementation plan:**
1. Add `driver` (`@ManyToOne User`) to `Order` entity
2. Add `DriverController`:
   - `GET /api/v1/driver/orders` — available orders for pickup
   - `POST /api/v1/driver/orders/{id}/accept` — accept delivery
   - `POST /api/v1/driver/orders/{id}/pickup` — mark as picked up
   - `POST /api/v1/driver/orders/{id}/deliver` — mark as delivered
3. Add delivery zone management (optional radius config)
4. Add driver earnings tracking
5. Add real-time location updates via WebSocket for customer tracking

**Expected outcome:** Full dispatch workflow. Customers can see driver assignment and real-time location. Drivers have a mobile-accessible API.

---

### 3.4 No customer-facing real-time delivery tracking

**Location:** `controller/OrderTrackingController.java` — basic structure exists, no real tracking

**What's wrong:** `OrderTrackingController` has `@MessageMapping("/orders/track")` but no actual driver location sharing, no ETA calculation, no map integration. Customer can see order status via polling but not real-time tracking.

**Real-world impact:** Customer expectations in 2024: every delivery shows a map with driver location and ETA (like UberEats, DoorDash). Without this, the system is perceived as low-quality.

**Implementation plan:**
1. Add `OrderTracking` entity: `orderId`, `driverLatitude`, `driverLongitude`, `lastUpdatedAt`
2. Create WebSocket topic `/topic/orders/{orderId}/tracking`
3. Add driver location update endpoint: `POST /api/v1/driver/location` (called by driver app every 5-10s)
4. Calculate ETA using distance + average speed from Google Maps API or simple Haversine formula
5. Customer frontend subscribes to `/topic/orders/{orderId}/tracking` and renders a map

**Expected outcome:** Customers can watch their delivery approach on a live map with ETA countdown. Comparable experience to UberEats/DoorDash.

---

### 3.5 No per-user coupon usage tracking

**Location:** `entity/Coupon.java` — has `usageLimit` (global) but no per-user tracking

**What's wrong:** A coupon with `usageLimit: 100` can be used 100 times by the SAME user. There's no `CouponUsage` entity to track per-user redemptions.

**Real-world impact:** "WELCOME10" coupon intended for new users — one user can use it 100 times. Direct revenue loss.

**Implementation plan:**
1. Create `CouponUsage` entity: `id`, `couponId`, `userId`, `orderId`, `usedAt`
2. Create `CouponUsageRepository`
3. Add `perUserUsageLimit` (Integer) to `Coupon` entity
4. Update `OrderService.placeOrder()`: before applying coupon, check `perUserUsageLimit` against `coupon_usage` table
5. Create `@Table(uniqueConstraints=...)` on `CouponUsage` to prevent (couponId, userId, orderId) duplicates

**Expected outcome:** Coupons respect per-user limits. "WELCOME10" can only be used once per user. Zero coupon abuse.

---

### 3.6 No structured menu item customizations

**Location:** `entity/MenuItem.java` — `customizations` is `@Column(columnDefinition = "TEXT")`

**What's wrong:** Customizations are stored as unstructured text. There's no way to:
- Define option groups (e.g., "Choose your side" with max 1)
- Set price modifiers per option (e.g., "Add bacon +$2")
- Make options required vs optional
- Validate against business rules

**Real-world impact:** Customers can write "add extra cheese, no onions, substitute fries" as freeform text. Kitchen can't parse this reliably. Wrong items prepared = waste + unhappy customers.

**Implementation plan:**
1. Create `MenuItemOptionGroup` entity: `id`, `menuItemId`, `name` (e.g., "Choose Side", "Extra Toppings"), `isRequired` (boolean), `maxSelections` (int, default 1), `sortOrder`
2. Create `MenuItemOption` entity: `id`, `optionGroupId`, `name` (e.g., "French Fries", "Side Salad"), `priceModifier` (BigDecimal), `isDefault` (boolean), `sortOrder`
3. Create DTOs, service, controller for admin CRUD
4. Update `CartService.addToCart()` to accept structured option IDs instead of text
5. Update `OrderService.placeOrder()` to calculate price modifiers from selected options
6. Update frontend to render option groups as radio buttons/checkboxes

**Expected outcome:** Full customization framework. No free-text customizations. Options are enforced at cart level, priced correctly, and send structured data to the kitchen.

---

### 3.7 No test coverage

**Location:** Only 3 test files exist: `SuperMalleApplicationTests.java`, `CouponEntityTest.java`, `IdempotencyInterceptorTest.java`

**What's wrong:** ~160 source files, 3 test files. No integration tests. No controller tests. No service tests. The `qa-tests/AdminControlTestSuite.py` is a Python file for an unrelated project.

**Real-world impact:** Every deployment is a leap of faith. Regression bugs will reach production. The QA audit found 103 issues — most would have been caught by tests.

**Implementation plan:**
1. **Phase A** — Unit tests for core services (priority):
   - `OrderServiceTest` — place order, cancel, review, status transitions (5 key scenarios)
   - `PaymentServiceTest` — create payment, handle success/failure, refund
   - `CartServiceTest` — add/remove items, clear cart
   - `AuthServiceTest` — register, login, rate limiting, password reset
2. **Phase B** — Repository integration tests with `@DataJpaTest`
3. **Phase C** — Controller integration tests with `@WebMvcTest` + `@MockBean`
4. **Phase D** — Security tests: unauthenticated access, role-based access, CORS
5. Target: minimum 60% line coverage on services, 80% on security

**Expected outcome:** Every deployment runs 100+ tests. Regressions caught within 2 minutes. Code quality gates prevent shipping broken code.

---

## 4. TIER 3 — MEDIUM (Missing Core Features)

### 4.1 No Receipt/Invoice Generation

**What's wrong:** After payment, customer gets nothing except a WebSocket notification. No PDF receipt, no email receipt, no printable invoice.

**Implementation plan:**
1. Add `ReceiptService` using iText or JasperReports for PDF generation
2. Generate receipt on `PaymentService.handlePaymentSuccess()`
3. Email receipt via `AsyncEmailService`
4. Add `GET /api/v1/orders/{id}/receipt` endpoint
5. Template includes: restaurant info, itemized list, tax breakdown, tip, payment method, order number, QR code

---

### 4.2 No Address Book / Multiple Delivery Addresses

**What's wrong:** `User` has no addresses. Every delivery order requires re-entering the address as a string.

**Implementation plan:**
1. Create `Address` entity: `id`, `userId`, `label` (Home/Work/Other), `addressLine1/2`, `city`, `state`, `zipCode`, `latitude`, `longitude`, `isDefault`, `deliveryInstructions`, `phone`
2. Add `@OneToMany` to `User` entity
3. Create `AddressRepository`, `AddressService`, `AddressController`
4. `PlaceOrderRequest` accepts `addressId` instead of raw address string
5. Add `/api/v1/addresses` endpoints: CRUD + set default

---

### 4.3 No Auto-Cancellation of Unpaid Orders

**What's wrong:** `OrderStatus.PENDING` orders with no payment sit forever.

**Implementation plan:**
1. Add `@Scheduled(cron = "0 */5 * * * *")` task to cancel PENDING orders older than 15 minutes
2. Add `PAYMENT_TIMEOUT_DURATION` config (default 15 min)
3. On cancellation: release any inventory holds, send notification, create status log entry
4. Add check in `PaymentService.createPaymentIntent()` to reject expired orders

---

### 4.4 No Order ETA Calculation

**What's wrong:** `estimatedReadyAt` can be set manually by admin but is never calculated.

**Implementation plan:**
1. Add `preparationTimeMinutes` to `MenuItem`
2. Calculate ETA = max(item prep times across all items) + base prep time + current queue depth * prep time per order
3. Auto-set `estimatedReadyAt` in `placeOrder()`
4. Expose via `GET /api/v1/orders/{id}` and WebSocket topic

---

### 4.5 No Refresh Token Mechanism

**What's wrong:** JWT expires after 24 hours with no way to refresh. User must re-login.

**Implementation plan:**
1. Create `RefreshToken` entity: `id`, `userId`, `token` (UUID), `expiresAt`, `isRevoked`, `deviceInfo`
2. Add `POST /api/v1/auth/refresh` endpoint
3. On refresh: validate refresh token, issue new JWT + new refresh token (rotation)
4. Revoke old refresh token on use (prevent replay attacks)
5. Add token cleanup scheduled task

---

### 4.6 No Account Lockout on Failed Logins

**What's wrong:** Rate limiting is per-IP only. Botnet with 1000 IPs can brute-force passwords.

**Implementation plan:**
1. Add `failedLoginAttempts` (int) and `lockedUntil` (LocalDateTime) to `User`
2. Increment on failed login, reset on successful login
3. Lock account for 30 minutes after 5 consecutive failures
4. Add admin unlock endpoint

---

### 4.7 DashboardService N+1 query performance

**Location:** `service/DashboardService.java` — queries each day individually

**What's wrong:** Getting a 365-day sales report fires 365 SQL queries.

**Implementation plan:**
1. Rewrite `getSalesReport()` to use single `GROUP BY DATE(order.created_at)` query
2. Add database-level aggregation instead of Java-level loops
3. Cache results with 15-minute TTL

---

## 5. TIER 4 — LOW (Production Polish)

### 5.1 Configuration Cleanup

**What's wrong:** Duplicate config keys in `application.yml`:
- `app.stripe.*` and `stripe.api.*` point to same values
- `app.restaurant.*` and `restaurant.*` point to same values

**Implementation plan:** Remove the non-prefixed duplicates (`stripe.*`, `restaurant.*`). Keep only `app.*` namespace.

### 5.2 Password Reset Token Security

**What's wrong:** Reset token is a UUID stored in plaintext in `PasswordResetToken` table.

**Implementation plan:** Hash the token with SHA-256 before storing. Send the original (unhashed) token in the email. On verification, hash the input and compare.

### 5.3 OpenAPI/Swagger Annotations

**What's wrong:** `OpenApiConfig.java` configures grouped APIs but no controller has `@Operation`, `@ApiResponses`, or `@Schema` annotations.

**Implementation plan:** Add `@Operation` (summary + description), `@ApiResponse` (response codes), `@Schema` (DTO examples) to all public endpoints.

### 5.4 Database Migration Tool

**What's wrong:** `spring.jpa.hibernate.ddl-auto=update` in production. Schema changes are untracked and irreversible.

**Implementation plan:** Add Flyway dependency, create `V1__init.sql` from current schema, disable `ddl-auto`, add `V2__` for changes, add migration testing to CI.

### 5.5 Duplicate SecurityConfig headers()

**Location:** `config/SecurityConfig.java`

**What's wrong:** Two `.headers()` blocks — first is dead code overridden by the second.

**Implementation plan:** Merge into single `.headers()` block.

### 5.6 H2 Console in Production

**What's wrong:** H2 console is accessible with ADMIN role in SecurityConfig — this should be profile-gated.

**Implementation plan:** Add `.requestMatchers("/h2-console/**").hasRole("ADMIN")` → wrap with `@Profile("dev")` annotation or only allow in dev profile.

### 5.7 Image Upload for Menu Items

**What's wrong:** `MenuItem.imageUrl` is a string with no upload endpoint.

**Implementation plan:** Add `POST /api/v1/admin/upload` endpoint that accepts multipart file, stores to S3/local filesystem, returns URL.

---

## 6. PHASE-BASED ROADMAP

```
PHASE A — FINANCIAL INTEGRITY (Week 1-2)
  ├── 2.1 ResilientPaymentService — wire real Stripe calls
  ├── 2.2 Order total calculation — fix tip, per-item tax, precision
  ├── 2.5 Stripe webhook — add missing event handlers
  └── 3.5 Coupon per-user tracking — CouponUsage entity

PHASE B — ORDER LIFECYCLE (Week 3-4)
  ├── 2.3 Operating hours validation
  ├── 2.4 Soft delete on all entities
  ├── 3.1 Kitchen Display System (KDS)
  ├── 3.2 Pre-order scheduling
  └── 4.3 Auto-cancellation of unpaid orders

PHASE C — DELIVERY & TRACKING (Week 5-6)
  ├── 3.3 Driver dispatch system
  ├── 3.4 Real-time delivery tracking
  └── 4.4 ETA calculation

PHASE D — NOTIFICATIONS & COMMUNICATIONS (Week 7-8)
  ├── 2.6 Email/SMS notification delivery
  ├── 4.1 Receipt/invoice generation
  └── 4.2 Address book

PHASE E — SECURITY & QUALITY (Week 9-10)
  ├── 3.7 Test coverage (minimum 60%)
  ├── 4.5 Refresh token mechanism
  ├── 4.6 Account lockout
  └── 5.x All Tier 4 items

PHASE F — POLISH (Week 11-12)
  ├── 4.7 Dashboard query optimization
  ├── 3.6 Structured customizations
  ├── 5.1 Config deduplication
  └── 5.7 Image upload
```

---

## 7. REAL-WORLD REASONING APPENDIX

### Why these gaps matter in production (not just code quality)

| Gap | Real Restaurant Impact |
|-----|----------------------|
| No operating hours check | Customer orders at 2AM → no food → chargeback + negative review |
| No tip support | Wait staff doesn't get paid → walk out → operations stop |
| Stub payment resilience | Stripe has 99.99% uptime. 0.01% of $1M/month revenue = $100 lost/month |
| No soft delete | Accidentally deleting a user = 3 years of financial records gone — audit fail |
| No KDS | Kitchen uses paper tickets → loses ticket → wrong food → wasted ingredients |
| No driver dispatch | Customer waits 2 hours for delivery → never orders again |
| No email receipt | Customer needs receipt for expense report → never uses app again |
| No test coverage | Every deploy is "pray and ship" — average 3 production bugs per deploy |
| No structured customization | "No onions, extra cheese, substitute fries" as TEXT → kitchen can't parse it |
| No auto-cancel | 50% of PENDING orders are abandoned → inventory held for 24 hours |

### Industry Standards (for comparison)

| Feature | Toast | Square | DoorDash | Our System |
|---------|-------|--------|----------|------------|
| Multi-location | ✅ | ✅ | ✅ | ❌ |
| Operating hours | ✅ | ✅ | ✅ | ❌ |
| Soft delete | ✅ | ✅ | N/A | ❌ |
| KDS | ✅ | ✅ | N/A | ❌ |
| Driver dispatch | ❌ | ❌ | ✅ | ❌ |
| Pre-ordering | ✅ | ✅ | ✅ | ❌ |
| Item options | ✅ | ✅ | ✅ | ❌ (TEXT) |
| Email receipts | ✅ | ✅ | ✅ | ❌ |
| Test coverage | 85%+ | 80%+ | 90%+ | <2% |
| Tax per item | ✅ | ✅ | ✅ | ❌ (flat) |
