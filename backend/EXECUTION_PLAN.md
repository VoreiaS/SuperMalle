# SUPERMALLE — FINAL EXECUTION PLAN
## Real-World Validated Implementation Strategy

> **Audit method:** Every feature was cross-referenced against real restaurant systems
> (Toast, Square, DoorDash, UberEats Merchant) and industry best-practice patterns.
> Existing code was classified as: ✅ CORRECT (keep), 🔧 NEEDS UPGRADE (refactor),
> ❌ STUB (rewrite), or 🆕 MISSING (build from scratch).

---

## PART 1: UPGRADE vs BUILD CLASSIFICATION

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ EXISTING CODE AUDIT — What's already there and what state it's in          │
├─────────────────────────────────────────────────────────────────────────────┤
│ Feature                    │ State       │ Action    │ Priority  │          │
├────────────────────────────┼─────────────┼───────────┼───────────┤          │
│ ResilientPaymentService    │ ❌ STUB     │ REWRITE   │ CRITICAL  │          │
│ EmailService               │ ✅ FULL     │ WIRE UP   │ CRITICAL  │          │
│ AsyncEmailService          │ ✅ FULL     │ +CONSUMER │ CRITICAL  │          │
│ RabbitMQ queues            │ ✅ CORRECT  │ KEEP      │ —         │          │
│ Order entity fields        │ ✅ PARTIAL  │ EXTEND    │ CRITICAL  │          │
│ StripeWebhookController    │ 🔧 PARTIAL  │ EXTEND    │ CRITICAL  │          │
│ NotificationService        │ ✅ CORRECT  │ KEEP      │ —         │          │
│ WebSocket/STOMP            │ ✅ CORRECT  │ KEEP      │ —         │          │
│ SecurityConfig             │ 🔧 EXTEND   │ REFACTOR  │ HIGH      │          │
│ LoginRateLimitService      │ 🔧 IN-MEM   │ REDISIFY  │ HIGH      │          │
│ OrderTrackingController    │ 🔧 SKELETON │ BUILD OUT │ HIGH      │          │
│ Coupon entity              │ 🔧 PARTIAL  │ EXTEND    │ HIGH      │          │
│ MenuItem.customizations    │ ❌ TEXT     │ REWRITE   │ HIGH      │          │
│ DashboardService           │ 🔧 N+1      │ OPTIMIZE  │ MEDIUM    │          │
│ FeatureFlagService         │ ✅ CORRECT  │ KEEP      │ —         │          │
│ AuditService               │ ✅ CORRECT  │ KEEP      │ —         │          │
│ Config duplication         │ ❌ DIRTY    │ CLEANUP   │ LOW       │          │
├────────────────────────────┼─────────────┼───────────┼───────────┤          │
│ MISSING FEATURES           │             │           │           │          │
├────────────────────────────┼─────────────┼───────────┼───────────┤          │
│ OperatingHours entity      │ 🆕 MISSING  │ BUILD     │ CRITICAL  │          │
│ Kitchen Display System     │ 🆕 MISSING  │ BUILD     │ HIGH      │          │
│ Driver dispatch            │ 🆕 MISSING  │ BUILD     │ HIGH      │          │
│ Delivery tracking          │ 🆕 MISSING  │ BUILD     │ HIGH      │          │
│ Structured customizations  │ 🆕 MISSING  │ BUILD     │ HIGH      │          │
│ Address book               │ 🆕 MISSING  │ BUILD     │ HIGH      │          │
│ Auto-cancel unpaid orders  │ 🆕 MISSING  │ BUILD     │ MEDIUM    │          │
│ ETA calculation            │ 🆕 MISSING  │ BUILD     │ MEDIUM    │          │
│ Receipt generation         │ 🆕 MISSING  │ BUILD     │ MEDIUM    │          │
│ Pre-order scheduling       │ 🆕 MISSING  │ BUILD     │ MEDIUM    │          │
│ Refresh tokens             │ 🆕 MISSING  │ BUILD     │ MEDIUM    │          │
│ Per-user coupon tracking   │ 🆕 MISSING  │ BUILD     │ HIGH      │          │
│ Account lockout            │ 🆕 MISSING  │ BUILD     │ HIGH      │          │
│ Test suite                 │ 🆕 MISSING  │ BUILD     │ HIGH      │          │
│ Database migrations        │ 🆕 MISSING  │ BUILD     │ MEDIUM    │          │
└─────────────────────────────────────────────┴───────────┴───────────┘
```

---

## PART 2: UPGRADE EXISTING FEATURES

### 1. WIRE UP EMAIL SERVICE (Critical — 0% of business flow emails sent)

**Current state:**
- `EmailService.java` — 9 methods, fully implemented (JavaMailSender + Thymeleaf HTML templates)
- 2 email templates exist: `order-confirmation.html`, `welcome.html`
- Templates for `password-reset`, `order-status-update`, `low-stock-alert`, `loyalty-points-earned`, `loyalty-tier-upgrade`, `order-modification-request`, `promotional` are **referenced but don't exist**
- ZERO services call `EmailService.sendXxx()` — not once in the entire codebase
- `AsyncEmailService` publishes to RabbitMQ but NO `@RabbitListener` exists to consume

**Real-world validation:** Every restaurant system sends:
- Order confirmation (immediately on placement)
- Status updates (preparing, ready, out-for-delivery)
- Password resets
- Receipts
- Marketing emails
Without these, the system has zero customer communication. Users get nothing after paying.

**Upgrade plan:**

```
Step 1: Create missing Thymeleaf templates (6 new HTML files)
  ├── templates/emails/password-reset.html
  ├── templates/emails/order-status-update.html
  ├── templates/emails/low-stock-alert.html
  ├── templates/emails/loyalty-points-earned.html
  ├── templates/emails/loyalty-tier-upgrade.html
  ├── templates/emails/order-modification-request.html
  └── templates/emails/promotional.html

Step 2: Wire EmailService into business services
  ├── AuthService.register()       → sendWelcomeEmail()
  ├── AuthService.forgotPassword() → sendPasswordResetEmail()
  ├── OrderService.placeOrder()    → sendOrderConfirmation()
  ├── OrderService.updateOrderStatus() → sendOrderStatusUpdate()
  ├── InventoryService             → sendLowStockAlert() on low stock
  ├── LoyaltyService               → sendLoyaltyPointsEarned() / sendLoyaltyTierUpgrade()

Step 3: Create RabbitMQ consumer
  ├── Create @RabbitListener(queues = "email.queue") EmailConsumer
  ├── Consumer deserializes EmailMessage, calls EmailService.sendHtmlEmail()
  ├── Configure retry: max 3 attempts → dead letter queue
  ├── Configure error handling: log + DLQ for permanent failures

Step 4: Create RabbitMQ consumers for other queues
  ├── NotificationConsumer (notification.queue) → push WebSocket + store in DB
  ├── OrderProcessingConsumer (order.processing.queue) → async order validation
  └── BackgroundJobConsumer (background.job.queue) → cleanup tasks
```

**File changes:**
- No new entities needed (EmailMessage DTO exists)
- 6 new HTML template files
- Add `EmailService` injection to: `AuthService`, `OrderService`, `InventoryService`, `LoyaltyService`
- Create `EmailConsumer.java`, `OrderProcessingConsumer.java` (2 new files)
- Add `@EnableRabbit` to app config if missing

---

### 2. FIX RESILIENT PAYMENT SERVICE (Critical — 100% stub)

**Current state:**
- `ResilientPaymentService.createPaymentIntentInternal()` returns `"pi_" + System.currentTimeMillis()` — a fake ID
- `ResilientPaymentService` is **never used** by `PaymentService` (which calls Stripe directly)
- The circuit breaker, retry, and timeout infrastructure exists around a function that does nothing

**Real-world validation:**
Stripe's API has 99.99% uptime but transient failures happen (network blips, rate limiting, 500s).
DoorDash/UberEats process millions in payments daily — they use circuit breakers + retries + idempotency.
Without resilience: a 5-second Stripe blip at lunch rush = $1000s in failed payments.

**Upgrade plan:**

```
Step 1: Remove mock from ResilientPaymentService
  └── Replace return "pi_" + ... with real PaymentIntent.create() call

Step 2: Integrate into payment flow
  └── PaymentService.createPaymentIntent() calls ResilientPaymentService
      instead of calling Stripe directly
  └── PaymentService.createPaymentIntent() → calls → ResilientPaymentService.createPaymentIntentWithTimeout()
      → circuit breaker → retry → time limiter → real Stripe API

Step 3: Add Stripe idempotency key
  └── Generate idempotency key from orderId + userId (already partially done)
  └── Pass to Stripe via params.putIdempotencyKey()

Step 4: Add fallback behaviour
  └── Circuit breaker OPEN → queue payment for retry → return "payment queued" response
  └── Admin dashboard shows queued payments with retry status
```

---

### 3. COMPLETE STRIPE WEBHOOK EVENT HANDLING (Critical — 4/9 events)

**Current state:** Handles `payment_intent.succeeded`, `payment_intent.payment_failed`, `charge.refunded` (stub), `checkout.session.completed` (stub). Missing: `charge.dispute.created`, `payment_intent.canceled`, `charge.refund.updated`.

**Upgrade plan:**

```
Add handlers:
  case "charge.dispute.created" → {
      PaymentService.handleDispute(paymentIntentId)
      → PaymentStatus.DISPUTED (new enum value needed)
      → notifyAdmin() via WebSocket
      → freeze order fulfillment
  }
  case "payment_intent.canceled" → {
      PaymentService.handlePaymentCancellation(paymentIntentId)
      → PaymentStatus.CANCELLED
      → release inventory hold
  }
  case "charge.refund.updated" → {
      PaymentService.handleRefundUpdate(refundId, newStatus)
      → update local Refund.status
  }

Fix edge-case bugs:
  - Return 500 (not 400) on deserialization failure → Stripe retries
  - Add PaymentStatus.DISPUTED to enum
  - Create RefundStatus.SUCCEEDED/FAILED enum (currently reusing PaymentStatus — wrong)
```

---

### 4. FIX ORDER TOTAL CALCULATION (Critical — missing tip, wrong tax)

**Current state:**
- `totalAmount = subtotal + tax + deliveryFee - discount` — tip is excluded
- Single flat tax rate for ALL items (alcohol ≠ food tax)
- `scale=2` precision causes rounding errors with fractional tax rates
- `tipAmount` field EXISTS on Order entity but is never set

**Upgrade plan:**

```
Step 1: Include tip in total
  └── Add tipAmount to PlaceOrderRequest DTO
  └── totalAmount = subtotal + tax + deliveryFee + tipAmount - discount
  └── OrderResponse builder: include tipAmount (currently missing)

Step 2: Per-item tax categories
  └── Add TaxCategory enum to MenuItem: STANDARD, ALCOHOL, PREPARED_FOOD, GROCERY
  └── Add taxRates config per category (e.g., ALCOHOL=0.10, STANDARD=0.08)
  └── OrderService.placeOrder(): calculate tax per OrderItem based on MenuItem.taxCategory
  └── Store itemized tax breakdown

Step 3: Fix precision (⚠️ schema migration needed)
  └── Change monetary fields from (10, 2) → (12, 4)
  └── Round to (10, 2) only at display/API response level
  └── RoundingMode.HALF_UP everywhere
```

---

### 5. EXTEND SECURITY CONFIG (High — 5 gaps)

**Current state:** JWT, CORS, CSRF, rate limiting all implemented. But:
- LoginRateLimitService is in-memory (lost on restart, doesn't work multi-instance)
- No per-account lockout (IP-based only, bypassable by botnet)
- No refresh tokens (JWT expires after 24h, user must re-login)
- JwtAuthenticationFilter swallows ALL exceptions as 403 instead of differentiating 401 vs 403
- X-Forwarded-For trusted without validation (rate limiting can be spoofed)

**Upgrade plan:**

```
Upgrade 1: Redis-backed rate limiting
  └── Replace ConcurrentHashMap in LoginRateLimitService with Redis + Bucket4j-Redis
  └── Also needed for RateLimitInterceptor (currently in-memory)

Upgrade 2: Add per-account lockout
  └── Add failedLoginAttempts + lockedUntil to User entity
  └── AuthService.login(): increment on failure, check before allowing
  └── 5 failures → lock for 30 minutes
  └── Admin unlock endpoint

Upgrade 3: Refresh tokens (new feature)
  └── Build from scratch (see missing features section)

Upgrade 4: Differentiate JWT exceptions
  └── ExpiredJwtException → 401 "Token expired"
  └── MalformedJwtException → 401 "Invalid token"
  └── SignatureException → 401 "Token tampered"
  └── Generic → 403 "Forbidden"

Upgrade 5: ForwardedHeaderFilter
  └── Add Spring's ForwardedHeaderFilter for trusted proxy headers
  └── Remove manual X-Forwarded-For parsing
```

---

### 6. EXTEND COUPON SYSTEM (High — missing per-user tracking)

**Current state:** Coupon entity has `usageLimit` (global) and `usageCount` (global) but no per-user tracking. A single user can consume all 100 uses of "WELCOME10".

**Upgrade plan:**

```
Add CouponUsage entity:
  └── id, coupon_id FK, user_id FK, order_id FK, used_at
  └── @Table(uniqueConstraints = (coupon_id, user_id, order_id))

Update OrderService.placeOrder():
  └── Before applying coupon, check couponUsageRepository
      .countByCouponIdAndUserId(coupon.getId(), userId) < coupon.getPerUserUsageLimit()
  └── After applying, save CouponUsage record

Add perUserUsageLimit field to Coupon entity (Integer, nullable = no limit)
```

---

## PART 3: BUILD NEW FEATURES

### 7. OPERATING HOURS (Critical — new entity + validation)

```
New entities:
  OperatingHours:
    id (Long, PK, auto)
    dayOfWeek (DayOfWeek enum: MONDAY-SUNDAY)
    openTime (LocalTime)
    closeTime (LocalTime)
    isClosed (boolean)
    createdAt, updatedAt

New files:
  OperatingHours entity, repository, service, DTOs
  AdminOperatingHoursController (CRUD)

Integration:
  OrderService.placeOrder():
    if no scheduledFor → validate now is within operating hours
    if scheduledFor → validate scheduledFor falls within operating hours

API endpoints:
  GET /api/v1/restaurant/hours  → public
  POST/PUT/DELETE /api/v1/admin/hours → admin CRUD
```

---

### 8. KITCHEN DISPLAY SYSTEM (High — 1 WebSocket + 4 endpoints)

```
New files:
  KitchenController (REST)
  KitchenWebSocketHandler (existing WebSocket extended)

Endpoints:
  GET  /api/v1/kitchen/orders → queue of CONFIRMED/PREPARING orders sorted oldest-first
  POST /api/v1/kitchen/orders/{id}/start → mark order PREPARING
  POST /api/v1/kitchen/orders/{id}/complete-item/{itemId} → per-item tracking
  POST /api/v1/kitchen/orders/{id}/ready → mark order READY

WebSocket topics:
  /topic/kitchen/orders (new order → kitchen)
  /topic/kitchen/orders/{id} (per-order updates)

Real-world: Toast KDS shows: order time → item list → prep time → SLA timer → bump button.
We replicate this with the order service + WebSocket + new endpoint.

Enhance NotificationService:
  Add "prepare by" SLA time = order.createdAt + max(item.preparationTimeMinutes)
```

---

### 9. DRIVER DISPATCH SYSTEM (High — new role + endpoints)

```
Extend Order entity:
  Add driver (@ManyToOne User, role = DELIVERY)

New files:
  DispatchController (driver-facing)

Endpoints:
  GET  /api/v1/driver/available-orders → READY orders within driver zone
  POST /api/v1/driver/orders/{id}/accept → assign driver to order
  POST /api/v1/driver/orders/{id}/pickup → mark OUT_FOR_DELIVERY
  POST /api/v1/driver/orders/{id}/deliver → mark DELIVERED
  POST /api/v1/driver/location → update GPS coordinates
  GET  /api/v1/driver/earnings → daily/weekly earnings

Add OrderStatus OUT_FOR_DELIVERY, DELIVERED (already exists in enum)

Real-world: DoorDash model — driver sees available orders within radius,
accepts → pickup → deliver flow. Simple but complete.
```

---

### 10. STRUCTURED MENU CUSTOMIZATIONS (High — replace TEXT blob)

```
New entities:
  MenuItemOptionGroup:
    id, menuItemId (FK), name, isRequired (boolean),
    maxSelections (int), sortOrder (int)

  MenuItemOption:
    id, optionGroupId (FK), name, priceModifier (BigDecimal),
    isDefault (boolean), sortOrder (int)

New files:
  OptionGroup entity, Option entity, repositories
  OptionGroupService, OptionGroupController (admin CRUD)
  OptionService, OptionController (admin CRUD)
  OptionGroupResponse, OptionResponse DTOs
  Update CartItem to store selectedOptionIds (JSON array)
  Update OrderItem to store selectedOptionNames + price modifiers

Real-world: Toast/Square allow "Choose your side: [Fries+$0] [Salad+$2] [Onion Rings+$3]"
Max 1, required=true. We replicate exactly this structure.
```

---

### 11. ADDRESS BOOK (High — replace string with entity)

```
New entity:
  Address:
    id, userId (FK), label (Home/Work/Other), addressLine1, addressLine2,
    city, state, zipCode, latitude, longitude, isDefault boolean,
    deliveryInstructions, phone, createdAt, updatedAt

New files:
  Address entity, repository, service, controller, DTOs
  Update PlaceOrderRequest to accept addressId OR inline address
  Update Order to store addressId (FK) instead of deliveryAddress (String)

Endpoints:
  GET/POST /api/v1/addresses
  GET/PUT/DELETE /api/v1/addresses/{id}
  PATCH /api/v1/addresses/{id}/default

Real-world: Every delivery app has saved addresses. Simple but essential UX.
```

---

### 12. AUTO-CANCEL UNPAID ORDERS (Medium — scheduled task)

```
New task:
  @Scheduled(cron = "0 */5 * * * *") → every 5 minutes
  Cancel orders WHERE status = PENDING AND createdAt < NOW() - 15 minutes
  Create OrderStatusLog entry
  Notify via WebSocket
  Release any inventory holds

Configurable timeout:
  app.order.payment-timeout-minutes = 15

Real-world: DoorDash auto-cancels unpaid orders after 10 min.
Waffle House (my real example) would have orders pile up forever without this.
```

---

### 13. REFRESH TOKENS (Medium — new auth flow)

```
New entity:
  RefreshToken:
    id, userId (FK), token (UUID, hashed), expiresAt,
    deviceInfo, isRevoked, createdAt

New endpoint:
  POST /api/v1/auth/refresh { refreshToken } → { accessToken, refreshToken }

Implementation:
  Refresh Token stored as bcrypt hash in DB (never plaintext)
  Rotation: each refresh invalidates old refresh token, issues new pair
  Expiry: 30 days for refresh token, 15 min for access token
  Revocation on logout or password change

Real-world: OAuth2 spec — short-lived access tokens + long-lived refresh tokens.
Standard in every modern auth system (Auth0, Clerk, Supabase).
```

---

### 14. ACCOUNT LOCKOUT (Medium — extend User entity)

```
Extend User entity:
  failedLoginAttempts (Integer, default 0)
  lockedUntil (LocalDateTime, nullable)

Extend AuthService.login():
  Check lockedUntil > now → throw "Account locked for X minutes"
  On failed login → increment failedLoginAttempts
  If failedLoginAttempts >= 5 → set lockedUntil = now + 30 minutes
  On successful login → reset failedLoginAttempts = 0, lockedUntil = null
  Send email: "Your account was accessed from a new device"

Admin unlock:
  POST /api/v1/admin/users/{id}/unlock

Real-world: Every site with >1000 users has this. Prevents credential stuffing.
```

---

### 15. TEST SUITE (High — build from zero)

```
Phase A — Service unit tests (20 tests, priority services):
  OrderServiceTest
  ├── placeOrder success path
  ├── placeOrder with empty cart → 400
  ├── placeOrder with expired coupon → 400
  ├── placeOrder optimistic lock retry (mock failure → retry succeeds)
  ├── cancelOrder while PREPARING → 400
  ├── addReview duplicate → 400
  └── updateOrderStatus invalid transition → 400

  PaymentServiceTest
  ├── createPaymentIntent success
  ├── createPaymentIntent duplicate → 400
  ├── handlePaymentSuccess sets order CONFIRMED
  ├── processRefund full
  ├── processRefund partial
  └── processRefund on FAILED payment → 400

  AuthServiceTest
  ├── register success
  ├── register duplicate email → 400
  ├── login with wrong password → 400
  └── forgotPassword creates token

  CartServiceTest
  ├── addToCart creates cart if none exists
  ├── addToCart increments quantity for duplicate
  └── clearCart removes all items

Phase B — Repository integration tests (10 tests):
  OrderRepositoryTest (@DataJpaTest + @AutoConfigureTestDatabase)
  UserRepositoryTest
  MenuItemRepositoryTest

Phase C — Security tests (5 tests):
  Unauthenticated access to /api/v1/orders → 401
  Customer accessing /api/v1/admin/orders → 403
  Admin accessing /api/v1/admin/orders → 200
  Public access to /api/v1/menu → 200
  Invalid JWT → 401

Phase D — API contract tests (5 tests):
  Verify all endpoints return correct status codes
  Verify all endpoints return standard ApiResponse envelope
```

---

## PART 4: REAL-WORLD VALIDATION TABLE

```
Feature                 │ How Toast/Square/DoorDash does it    │ Our approach
────────────────────────┼──────────────────────────────────────┼──────────────────────────
Email notifications     │ SendGrid/Mailgun API → transactional │ JavaMailSender + Thymeleaf
                        │  emails via template system          │ + RabbitMQ async
────────────────────────┼──────────────────────────────────────┼──────────────────────────
Payment resilience      │ Payment service with retry +         │ Resilience4j circuit
                        │  circuit breaker + idempotency keys  │  breaker + retry + timeout
────────────────────────┼──────────────────────────────────────┼──────────────────────────
Stripe webhooks         │ 9+ events handled, all idempotent    │ Same 9 events,
                        │  DLQ for failed events               │  idempotency via event ID
────────────────────────┼──────────────────────────────────────┼──────────────────────────
Order totals            │ Itemized receipt with per-item tax,  │ Per-item tax via TaxCategory
                        │  tip line, subtotal                  │  enum, tip included
────────────────────────┼──────────────────────────────────────┼──────────────────────────
Operating hours         │ Single source of truth, validated    │ OperatingHours entity +
                        │  on every order placement            │  placeOrder() validation
────────────────────────┼──────────────────────────────────────┼──────────────────────────
Kitchen Display         │ WebSocket-pushed tickets with        │ WebSocket + REST endpoints
                        │  SLA timers + bump/complete          │  + item-level tracking
────────────────────────┼──────────────────────────────────────┼──────────────────────────
Driver dispatch         │ Driver app → accept → pickup →       │ Same flow via REST API
                        │  deliver → GPS tracking              │  + location endpoint
────────────────────────┼──────────────────────────────────────┼──────────────────────────
Customizations          │ Option groups with max/min,          │ MenuItemOptionGroup +
                        │  price modifiers, defaults           │  MenuItemOption entities
────────────────────────┼──────────────────────────────────────┼──────────────────────────
Refresh tokens          │ Access token 15min + refresh token   │ Rotation-based, stored
                        │  30 days, rotation on use            │  hashed, revocable
────────────────────────┼──────────────────────────────────────┼──────────────────────────
Rate limiting           │ Redis-backed token bucket,           │ Bucket4j + Redis
                        │  per-user + per-IP                   │  + per-account lockout
────────────────────────┼──────────────────────────────────────┼──────────────────────────
Database migrations     │ Flyway/Liquibase, versioned SQL      │ Flyway, Hibernate ddl-auto
                        │  never ddl-auto in production        │  → disabled in production
────────────────────────┼──────────────────────────────────────┼──────────────────────────
Test coverage           │ 80%+ service layer, 70%+ controller  │ Same target:
                        │  CI gates on coverage                │  60% services, tests in CI
```

---

## PART 5: EXECUTION ORDER (Dependency-Aware)

```
Phase 0 — FOUNDATION (Week 1) — No new features, just wiring existing code
  ├── 1. Wire EmailService into: AuthService, OrderService, InventoryService, LoyaltyService
  ├── 2. Create @RabbitListener consumers for email.queue + notification.queue
  ├── 3. Create missing email templates
  ├── 4. Fix OrderResponse to include tipAmount
  └── 5. Fix config duplication (remove stripe.*, restaurant.*, keep app.*)

Phase 1 — PAYMENT INTEGRITY (Week 2) — Money flow must be correct
  ├── 1. Real Stripe calls in ResilientPaymentService
  ├── 2. Integrate ResilientPaymentService into PaymentService flow
  ├── 3. Add missing Stripe webhook event handlers
  ├── 4. Add PaymentStatus.DISPUTED
  └── 5. Create RefundStatus enum (decouple from PaymentStatus)

Phase 2 — ORDER INTEGRITY (Week 3) — Order lifecycle must be correct
  ├── 1. Per-item tax categories
  ├── 2. Fix monetary precision (10,2) → (12,4)
  ├── 3. OperatingHours entity + validation
  ├── 4. CouponUsage entity + per-user tracking
  ├── 5. Auto-cancel unpaid orders (scheduled task)

Phase 3 — OPERATIONS (Weeks 4-5) — Staff workflows
  ├── 1. Kitchen Display System (KDS)
  ├── 2. Structured menu customizations
  ├── 3. ETA calculation (prep time + queue depth)
  └── 4. Address book

Phase 4 — DELIVERY (Weeks 6-7) — Customer-facing tracking
  ├── 1. Driver dispatch system
  ├── 2. Real-time delivery tracking (WebSocket GPS)
  ├── 3. Pre-order scheduling
  └── 4. Receipt generation (PDF + email)

Phase 5 — SECURITY (Week 8) — Hardening
  ├── 1. Redis-backed rate limiting
  ├── 2. Account lockout
  ├── 3. Refresh tokens
  ├── 4. Differentiate JWT exceptions (401 vs 403)
  ├── 5. H2 console gated behind dev profile
  └── 6. ForwardedHeaderFilter

Phase 6 — QUALITY (Weeks 9-10) — Testing + DB migrations
  ├── 1. Service unit tests (20+ tests)
  ├── 2. Repository integration tests (10+ tests)
  ├── 3. Security controller tests (5+ tests)
  ├── 4. Flyway migration setup
  ├── 5. DashboardService query optimization
  └── 6. Image upload endpoint

Phase 7 — POLISH (Week 11) — Cleanup
  ├── 1. OpenAPI @Operation annotations on all controllers
  ├── 2. Config cleanup (merge duplicate keys)
  ├── 3. Merge duplicate .headers() in SecurityConfig
  └── 4. Soft delete unified base entity
```

---

## PART 6: WHAT REMAINS UNCHANGED (Already production-grade)

These features were audited and found to match real-world standards. **Do not touch:**

| Feature | Why it's correct |
|---------|-----------------|
| JWT auth (jjwt 0.12.6) | Industry standard, 256-bit HMAC, configurable expiry |
| BCrypt password hashing | Strength 12, per-security-best-practice |
| AES-256 PII encryption | JPA AttributeConverters for phone/address/credit card |
| CORS configurability | Whitelist via env var, no wildcards with credentials |
| Resilience4j configs | 5 circuit breakers, 4 retry configs, 3 time limiters |
| RabbitMQ DLQ architecture | Each queue has DLX + DLQ, proper dead-letter routing |
| Redis cache with per-cache TTLs | menuItems=2h, categories=2h, inventory=5min, userLoyalty=10min |
| WebSocket STOMP | Proper broker relay, /topic and /queue routing |
| Feature flag system | Percentage rollouts, user targeting, Redis-cached |
| Audit logging | Full AOP-based, all admin actions captured |
| Rate limiting framework | Per-role limits (100/500/1000 rpm), Bucket4j |
| Idempotency layer | SHA-256 request hashing, AOP interceptor, TTL cleanup |
| Global exception handler | Handles 15+ exception types with structured JSON responses |
| Custom health indicators | 6 indicators: DB, Redis, RabbitMQ, Stripe, liveness, readiness |
| Order service optimistic locking | 3 retries with exponential backoff + jitter |
| Coupon system base | Discount types, min order, expiry, usage limits (just missing per-user) |
| Loyalty program | Full tier system, points earn/redeem, referrals, leaderboard |
| Inventory management | Track, restock, low-stock alerts, overstock detection |
| Order modification flow | Customer requests, admin approve/reject, proper status tracking |
