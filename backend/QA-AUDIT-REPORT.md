# QUALITY ASSURANCE AUDIT REPORT
# SuperMalle Restaurant System — Spring Boot 4.0.5 / Java 21
# Audit Date: 2026-04-15
# Auditor: QA Agent (Automated Deep Code Review)

═══════════════════════════════════════════════════════════════
 EXECUTIVE SUMMARY
═══════════════════════════════════════════════════════════════

Total Findings: 103
CRITICAL: 12 | HIGH: 31 | MEDIUM: 38 | LOW: 22

Verdict: NOT PRODUCTION READY. Critical gaps in security,
financial data integrity, real-world workflow coverage,
and data model completeness must be addressed before any
production deployment.

═══════════════════════════════════════════════════════════════
 PHASE 1: DATA MODEL & ENTITY AUDIT
═══════════════════════════════════════════════════════════════

--- ENUM GAPS ---

[1] [HIGH] OrderStatus missing key real-world states
  Current: PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, COMPLETED, CANCELLED
  Missing: REFUNDED (distinct from CANCELLED when payment captured), ON_HOLD (kitchen backlog), SCHEDULED (pre-orders), READY_FOR_PICKUP (pickup-specific)
  FIX: Add REFUNDED, ON_HOLD, SCHEDULED, READY_FOR_PICKUP to OrderStatus enum

[2] [HIGH] OrderType missing DINE_IN
  FIX: Add DINE_IN to OrderType. Add tableNumber field to Order when type is DINE_IN

[3] [MEDIUM] DiscountType missing FREE_DELIVERY and BUY_X_GET_Y
  FIX: Add FREE_DELIVERY, BUY_X_GET_Y. These are standard restaurant coupon types

[4] [MEDIUM] UserRole missing KITCHEN_STAFF and MANAGER
  FIX: Add KITCHEN_STAFF (kitchen display access), MANAGER (intermediate admin). Rename DELIVERY to DRIVER

[5] [HIGH] Refund reuses PaymentStatus — nonsensical
  A Refund with status "REFUNDED" makes no sense. "PARTIALLY_REFUNDED" on a Refund is also wrong.
  FIX: Create RefundStatus enum: PENDING, PROCESSING, SUCCEEDED, FAILED, CANCELLED

[6] [LOW] PaymentStatus missing DISPUTED
  FIX: Add DISPUTED for Stripe chargebacks

--- USER ENTITY ---

[7] [CRITICAL] User.passwordHash leaks via JSON serialization
  Any API response including a User object will expose the bcrypt hash.
  FIX: Add @JsonProperty(access = Access.WRITE_ONLY) on passwordHash

[8] [HIGH] User missing email verification and password reset
  Without these, users cannot verify emails or reset passwords — standard on every auth system.
  FIX: Add fields: emailVerified (Boolean), emailVerificationToken (String), emailVerificationTokenExpiry (LocalDateTime), passwordResetToken (String), passwordResetTokenExpiry (LocalDateTime)

[9] [HIGH] User missing delivery address information
  A restaurant delivery system without customer addresses requires re-entry on every order.
  FIX: Create Address entity with @OneToMany from User (label, addressLine1, addressLine2, city, state, zipCode, latitude, longitude, isDefault, deliveryInstructions)

[10] [MEDIUM] User missing soft delete and last login tracking
  FIX: Add deletedAt (LocalDateTime), lastLoginAt (LocalDateTime). isActive alone is insufficient

[11] [MEDIUM] User.phone missing unique constraint and format validation
  FIX: Add @Column(unique = true) on phone. Add @Pattern for E.164 format

[12] [MEDIUM] User fields missing size constraints
  FIX: name=@Column(length=100), email=@Column(length=255), passwordHash=@Column(length=72), phone=@Column(length=20)

[13] [LOW] User.stripeCustomerId missing unique constraint
  FIX: Add @Column(unique = true) to prevent two users referencing same Stripe customer

--- CART ENTITY ---

[14] [HIGH] Cart.items FetchType.EAGER — N+1 performance killer
  FIX: Change to LAZY. Use @EntityGraph in repository when items are needed

[15] [LOW] Cart missing coupon and delivery preferences
  FIX: Add couponCode, preferredOrderType, scheduledFor fields

--- CARTITEM ENTITY ---

[16] [HIGH] CartItem missing @NotNull on required fields
  FIX: Add @NotNull on cart, menuItem, unitPrice. Null unitPrice causes NPE in totals

[17] [MEDIUM] CartItem.quantity missing @Min validation
  FIX: Add @Min(1) on quantity. Zero or negative quantities corrupt order totals

[18] [MEDIUM] CartItem.customizations is unstructured TEXT
  FIX: Use @JdbcTypeCode(SqlTypes.JSON) with structured type, or create CartItemCustomization entity

--- CATEGORY ENTITY ---

[19] [MEDIUM] Category.name missing unique constraint and size limit
  FIX: @Column(nullable=false, unique=true, length=100). Duplicate categories confuse menu filtering

[20] [MEDIUM] Category missing audit fields and parent hierarchy
  FIX: Add createdAt, updatedAt. Add parentCategory for subcategories

[21] [LOW] Category missing slug for URL-safe identifiers
  FIX: Add @Column(unique=true, length=100) String slug

--- COUPON ENTITY ---

[22] [HIGH] Coupon missing per-user usage limit
  One user could consume all uses of a "WELCOME10" coupon.
  FIX: Add perUserUsageLimit (Integer). Add CouponUsage entity to track per-user usage

[23] [MEDIUM] Coupon.code missing size/format constraint
  FIX: @Column(nullable=false, unique=true, length=50) + @Pattern(regexp="^[A-Z0-9_-]+$")

[24] [MEDIUM] Coupon missing applicability rules
  FIX: Add applicableCategories (JSON), applicableMenuItemIds (JSON), applicableOrderType. Without these, "Free Delivery" coupon applies to pickup orders too

[25] [LOW] Coupon missing updatedAt
  FIX: Add @UpdateTimestamp LocalDateTime updatedAt

--- MENUITEM ENTITY ---

[26] [CRITICAL] MenuItem missing allergen information — FOOD SAFETY LIABILITY
  Not surfacing allergen info is a legal liability for a restaurant system.
  FIX: Add allergens field as @ElementCollection with Allergen enum (PEANUTS, DAIRY, GLUTEN, SHELLFISH, TREE_NUTS, SOY, EGGS, FISH)

[27] [HIGH] MenuItem missing dietary tags
  FIX: Add @ElementCollection dietaryTags (VEGAN, VEGETARIAN, GLUTEN_FREE, HALAL, KOSHER)

[28] [HIGH] MenuItem.customizations is unstructured TEXT instead of proper entities
  FIX: Create MenuItemOptionGroup (name, isRequired, maxSelections) + MenuItemOption (name, priceModifier, isDefault) entities. TEXT blob is unusable for business rules

[29] [MEDIUM] MenuItem missing cost tracking
  FIX: Add costPrice (BigDecimal) for margin reporting, sku (String) for inventory integration

[30] [MEDIUM] MenuItem missing tax category
  FIX: Add taxCategory (String) or taxRate (BigDecimal). Alcohol taxed differently than food

[31] [LOW] MenuItem missing isFeatured and sortOrder
  FIX: Add isFeatured (Boolean), sortOrder (Integer)

--- ORDER ENTITY ---

[32] [CRITICAL] Order missing tipAmount/gratuity field
  Tips are a core revenue stream. totalAmount formula is wrong without it.
  FIX: Add tipAmount (BigDecimal). Formula: totalAmount = subtotalAmount + taxAmount + deliveryCharge + tipAmount - discountAmount

[33] [CRITICAL] Order missing taxAmount field
  Tax must be shown separately on receipts for legal compliance.
  FIX: Add taxAmount (BigDecimal). Baking tax into totalAmount makes tax reporting impossible

[34] [HIGH] Order missing subtotalAmount field
  FIX: Add subtotalAmount (BigDecimal). You cannot reconstruct price breakdown without it

[35] [HIGH] Order missing delivery driver assignment
  FIX: Add @ManyToOne User driver. Without this, dispatch is impossible

[36] [HIGH] Order.paymentMethod is String instead of enum
  FIX: Create PaymentMethod enum (CREDIT_CARD, DEBIT_CARD, CASH, APPLE_PAY, GOOGLE_PAY). Strings are unqueryable

[37] [HIGH] Order missing @NotNull on critical fields
  FIX: Add @NotNull on user, orderType, totalAmount, status

[38] [MEDIUM] Order missing delivery geolocation
  FIX: Add deliveryLatitude, deliveryLongitude for map tracking

[39] [MEDIUM] Order missing individual status timestamps for SLA
  FIX: Add confirmedAt, preparingAt, readyAt, outForDeliveryAt. Enables SLA queries without joining status logs

[40] [MEDIUM] Order missing cancellationReason and scheduledFor
  FIX: Add cancellationReason (required when CANCELLED), scheduledFor (pre-orders)

[41] [MEDIUM] Order.deliveryAddress missing size constraint
  FIX: @Column(length=500)

[42] [LOW] Order.orderNumber generation not collision-safe
  FIX: Use database sequence or UUID. Add @Column(unique=true, length=30)

[43] [LOW] Order.statusLog @OneToMany missing orphanRemoval
  FIX: Add orphanRemoval=true

--- ORDERITEM ENTITY ---

[44] [HIGH] OrderItem missing @NotNull on critical fields
  FIX: @NotNull on order, unitPrice, subtotal, menuItemName

[45] [MEDIUM] OrderItem missing per-item specialInstructions
  FIX: Add @Column(length=500) String specialInstructions

[46] [LOW] OrderItem.subtotal should have @Column(nullable=false)
  FIX: Add @Column(nullable=false, precision=10, scale=2)

--- ORDERSTATUSLOG ---

[47] [HIGH] OrderStatusLog.changedBy is String instead of User reference
  FIX: Change to @ManyToOne User changedBy. Keep String as changedByName for display

[48] [MEDIUM] OrderStatusLog missing previousStatus field
  FIX: Add @Enumerated OrderStatus previousStatus. Prevents needing to query previous log entry

[49] [MEDIUM] OrderStatusLog missing @NotNull constraints
  FIX: @NotNull on order, status

--- PAYMENT ENTITY ---

[50] [HIGH] Payment.metadata is String instead of Map
  FIX: Use @JdbcTypeCode(SqlTypes.JSON) Map<String, String> metadata

[51] [MEDIUM] Payment missing failure information
  FIX: Add failureCode (String), failureMessage (TEXT) for Stripe error codes

[52] [MEDIUM] Payment missing tipAmount breakdown
  FIX: Add tipAmount (BigDecimal) for Stripe tip reconciliation

[53] [LOW] Payment.stripePaymentMethodId missing size constraint
  FIX: @Column(length=100)

--- REFUND ENTITY ---

[54] [HIGH] Refund.status should use dedicated RefundStatus enum (see #5)
  FIX: Create and use RefundStatus enum

[55] [MEDIUM] Refund missing stripeBalanceTransactionId and refundedBy
  FIX: Add stripeBalanceTransactionId (for reconciliation), @ManyToOne User refundedBy (audit trail)

[56] [MEDIUM] Refund.reason missing @NotNull
  FIX: @Column(nullable=false, length=500)

--- REVIEW ENTITY ---

[57] [HIGH] Review missing uniqueness constraint — unlimited reviews per order
  FIX: @Table(uniqueConstraints=@UniqueConstraint(columnNames={"user_id","order_id"}))

[58] [MEDIUM] Review missing restaurant response fields
  FIX: Add response (String), respondedAt (LocalDateTime)

[59] [MEDIUM] Review.rating missing @NotNull
  FIX: @NotNull + @Column(nullable=false)

[60] [LOW] Review missing moderation fields
  FIX: Add isFlagged (Boolean), flaggedReason (String)

--- SETTINGS ENTITY ---

[61] [MEDIUM] Settings missing createdAt
  FIX: Add @CreationTimestamp LocalDateTime createdAt

[62] [MEDIUM] Settings.value is untyped — no validation
  FIX: Add type field (STRING, NUMBER, BOOLEAN, JSON) with @PrePersist validation

--- MISSING ENTITIES ---

[63] [CRITICAL] No Restaurant/Location entity exists
  The system is hardcoded to a single restaurant. Multi-location is baseline in Toast/Square.
  FIX: Create Restaurant entity (name, address, operatingHours, latitude, longitude, phone, email, logoUrl, isActive). Every entity needs restaurant_id FK

[64] [HIGH] No Address entity for user delivery addresses
  FIX: Create Address entity (label, addressLine1/2, city, state, zipCode, lat, lng, isDefault, deliveryInstructions)

[65] [HIGH] No MenuItemOptionGroup/MenuItemOption entities
  FIX: Create structured customization entities (see #28). TEXT customizations cannot enforce business rules

[66] [MEDIUM] No OperatingHours entity
  FIX: Create OperatingHours (restaurant_id, dayOfWeek, openTime, closeTime, isClosed). Without this, orders can be placed when restaurant is closed

[67] [MEDIUM] No Notification entity for persistence
  FIX: Create Notification (user_id, type, title, message, isRead, createdAt). WebSocket notifications are fire-and-forget with no record

[68] [LOW] No AuditLog entity
  FIX: Create AuditLog for PCI/SOC2 compliance tracking

--- REPOSITORY ISSUES ---

[69] [HIGH] CartItemRepository delete methods missing @Modifying/@Transactional
  FIX: Add @Modifying @Transactional on deleteByCartIdAndId, deleteAllByCartId

[70] [HIGH] ReviewRepository missing existsByUserIdAndOrderId
  FIX: Add boolean existsByUserIdAndOrderId(Long userId, Long orderId)

[71] [MEDIUM] CouponRepository missing expiry-aware query
  FIX: findByCodeAndIsActiveTrue doesn't check expiry. Add expired check or new query method

[72] [MEDIUM] UserRepository missing key lookups
  FIX: Add findByPhone, findByStripeCustomerId, findByPasswordResetToken

[73] [MEDIUM] OrderRepository JPQL uses string literal for enum
  FIX: Parameterize 'COMPLETED' enum comparison instead of hardcoded string

[74] [LOW] No @EntityGraph definitions on any repository
  FIX: Add @EntityGraph on CartRepository.findByUserId, OrderRepository.findById to prevent N+1

[75] [LOW] OrderRepository missing driver-specific queries
  FIX: Add findByDriverIdOrderByCreatedAtDesc, findByStatusAndOrderType

--- CROSS-CUTTING DATA INTEGRITY ---

[76] [CRITICAL] No soft delete mechanism on any entity
  Hard-deleting a User cascades/corrupts Orders, Payments, Reviews.
  FIX: Add deletedAt to User, MenuItem, Category, Coupon. Use @Where(clause="deleted_at IS NULL") or Hibernate @SoftDelete

[77] [HIGH] BigDecimal monetary fields use precision=10, scale=2
  scale=2 causes rounding errors in tax calculations (e.g., 8.625% tax).
  FIX: Use scale=4 for calculation fields, round to scale=2 only at display

[78] [HIGH] No Restaurant-level FK on any entity
  All data is globally mixed — cannot support multi-restaurant deployment.
  FIX: Add restaurant_id FK on Order, MenuItem, Category, Coupon, User (at minimum)

[79] [MEDIUM] No cart-to-order reference for audit trail
  FIX: Add @OneToOne Cart sourceCart on Order for pricing discrepancy debugging

[80] [MEDIUM] No database-level index annotations on frequently queried columns
  FIX: Add @Table(indexes={...}) on Order (user_id, status, createdAt), MenuItem (category_id, isAvailable), Payment (orderId, status), Review (menuItemId, userId)


═══════════════════════════════════════════════════════════════
 PHASE 2: SERVICE & BUSINESS LOGIC AUDIT
═══════════════════════════════════════════════════════════════

--- FINANCIAL / ORDER CALCULATION ---

[81] [CRITICAL] Order total calculation is wrong — missing subtotal, tax, tip breakdown
  OrderService.placeOrder() computes: totalAmount = subtotal + tax + deliveryFee - discount
  Missing: tipAmount, subtotalAmount, taxAmount stored on Order entity.
  The subtotal is computed in-memory and discarded. Tax calculation uses a single rate for all items.
  FIX: Store subtotalAmount, taxAmount, tipAmount on Order. Support per-item tax categories

[82] [HIGH] CartService tax calculation assumes flat rate for all items
  Real restaurants have different tax rates (alcohol ~10%, food ~6%, merchandise varies).
  FIX: Add taxCategory to MenuItem. Calculate tax per item based on category

[83] [HIGH] Race condition on cart — two concurrent requests can add duplicate items
  No locking or version check on Cart. Two POST /cart/items with same menuItemId could create duplicates instead of incrementing.
  FIX: Add @Version to Cart entity. Or use database-level SELECT FOR UPDATE

[84] [HIGH] OrderService.placeOrder() doesn't verify restaurant is open
  FIX: Check OperatingHours before accepting order. Return error if outside business hours

[85] [MEDIUM] OrderService doesn't validate menu item availability at order time
  isAvailable is checked in CartService.addToCart, but between adding to cart and placing order, the item could be marked unavailable.
  FIX: Re-validate all cart items' availability in placeOrder()

[86] [MEDIUM] Coupon validation is incomplete
  Missing checks: per-user usage limit, applicable categories/items, applicable order type
  FIX: Add CouponUsage tracking entity. Validate applicability rules

[87] [MEDIUM] No order timeout/auto-cancellation
  A PENDING order with no payment should auto-cancel after X minutes.
  FIX: Add scheduled task to cancel unpaid orders after configurable timeout (e.g., 15 minutes)

[88] [MEDIUM] No estimated delivery time calculation
  FIX: Calculate based on order items' preparationTimeMinutes + current kitchen load + delivery distance

[89] [LOW] DashboardService.getSalesReport queries each day individually
  365 days = 365 DB queries. Very slow.
  FIX: Use a single GROUP BY DATE query instead of per-day loops

--- SECURITY ---

[90] [CRITICAL] PaymentController directly injects OrderRepository
  Controller should not directly access repositories — this bypasses service-layer validation.
  FIX: Move IDOR check into PaymentService.getPaymentByOrderId()

[91] [CRITICAL] CategoryController exposes CategoryRequest import but has no POST/PUT/DELETE
  CategoryRequest is imported but unused. Customer CategoryController only has GET.
  FIX: Remove unused import. CategoryRequest is admin-only

[92] [HIGH] X-Forwarded-For header is trusted without validation
  AuthController.getClientIpAddress() trusts X-Forwarded-For without verification.
  An attacker can spoof this to bypass rate limiting.
  FIX: Only trust X-Forwarded-For if request comes from a known proxy IP. Or use Spring ForwardedHeaderFilter

[93] [HIGH] LoginRateLimitService uses in-memory ConcurrentHashMap
  Buckets are lost on restart. In multi-instance deployment, each instance has separate buckets.
  FIX: Use Redis-backed Bucket4j or distributed rate limiter for multi-instance

[94] [HIGH] JwtAuthenticationFilter swallows all auth exceptions silently
  catch(Exception e) logs error but continues the filter chain. A malformed token gets a 403 instead of 401.
  FIX: Differentiate ExpiredJwtException (401 with message) from MalformedJwtException (401 with different message) from generic errors

[95] [HIGH] No account lockout after repeated failed logins
  Rate limiting is per-IP. A botnet with different IPs can brute-force individual accounts.
  FIX: Add per-account lockout (e.g., 5 failures on same email = 30-minute lockout)

[96] [MEDIUM] SecurityConfig allows H2 console access with ADMIN role
  H2 console should never be accessible in production — it allows arbitrary SQL execution.
  FIX: Add @Profile("!postgres") on the H2 console route or remove entirely in production

[97] [MEDIUM] No refresh token mechanism
  JWT expiration forces re-login. No way to extend session without full re-auth.
  FIX: Implement refresh tokens stored in database with rotation

[98] [MEDIUM] WebSocket authentication only checks STOMP CONNECT
  Subsequent SUBSCRIBE/SEND frames are not authenticated.
  FIX: Validate authorization on all STOMP commands, not just CONNECT

[99] [MEDIUM] AdminNotificationController.AnnouncementRequest missing @Valid
  No validation on message field — could be empty or megabytes long.
  FIX: Add @Valid + @NotBlank @Size(max=1000) on message

[100] [MEDIUM] ChangePasswordRequest doesn't validate new password strength
  FIX: Add @Size(min=8) + pattern validation for password complexity

--- STRIPE / PAYMENT ---

[101] [HIGH] StripeWebhookController doesn't handle all critical event types
  Missing: charge.dispute.created (chargebacks), payment_intent.canceled, charge.refunded, charge.refund.updated
  FIX: Add handlers for dispute, cancellation, and refund events

[102] [HIGH] No idempotency on PaymentIntent creation
  If client retries due to network timeout, duplicate PaymentIntents are created.
  FIX: Use Stripe idempotency keys based on orderId

[103] [MEDIUM] Payment metadata field stores clientSecret
  Payment.metadata stores the PaymentIntent clientSecret — this is sensitive data that should not be in metadata column.
  FIX: Store clientSecret in a dedicated field or return it only from createPaymentIntent, don't persist

[104] [MEDIUM] StripeWebhookController deserialization failure is logged but not retried
  If deserialization fails, the event is silently dropped.
  FIX: Return 500 so Stripe retries. Only return 200 for successfully processed events

[105] [LOW] StripeConfig sets API key as static field
  Stripe.apiKey = secretKey is a static singleton — breaks multi-tenant scenarios.
  FIX: Use StripeClient instance instead of static API key

--- ERROR HANDLING ---

[106] [HIGH] GlobalExceptionHandler doesn't handle DataIntegrityViolationException
  Duplicate email, unique constraint violations will return 500 with raw SQL in response.
  FIX: Add @ExceptionHandler(DataIntegrityViolationException.class) returning 409 CONFLICT with sanitized message

[107] [HIGH] GlobalExceptionHandler doesn't handle ConstraintViolationException
  JPA @NotNull violations at the persistence layer will return 500.
  FIX: Add @ExceptionHandler(ConstraintViolationException.class) returning 400 BAD_REQUEST

[108] [MEDIUM] PaymentException doesn't have its own handler
  Falls through to generic Exception handler which returns 500.
  FIX: Add @ExceptionHandler(PaymentException.class) returning 402 PAYMENT_REQUIRED or 500 with payment-specific error format

[109] [MEDIUM] NotificationService silently swallows WebSocket errors
  No try-catch around messagingTemplate.convertAndSend(). If WebSocket broker is down, the entire order placement could fail.
  FIX: Wrap WebSocket sends in try-catch. Log errors but don't fail the business operation

[110] [MEDIUM] No retry mechanism for Stripe API calls
  Network timeouts or transient Stripe errors cause immediate PaymentException.
  FIX: Add @Retryable on PaymentService methods calling Stripe API

--- DATA INITIALIZER ---

[111] [CRITICAL] Hardcoded admin credentials in source code
  admin@supermalle.com / admin123 is hardcoded and logged to console.
  FIX: Use environment variables for admin credentials. Never log passwords

[112] [MEDIUM] DataInitializer runs every startup even when data exists
  Uses count() > 0 check which is fragile (what if categories were deleted?).
  FIX: Check for specific admin user existence, not count. Add @Profile("dev") to prevent seeding in production

[113] [LOW] Category.description parameter not passed in DataInitializer
  saveCategory() accepts description but it's not used in the method
  FIX: Pass and store description when seeding categories


═══════════════════════════════════════════════════════════════
 PHASE 3: CONTROLLER & API AUDIT
═══════════════════════════════════════════════════════════════

[114] [MEDIUM] PaymentController.getPaymentByOrderId IDOR check is in controller, not service
  FIX: Move ownership verification into PaymentService

[115] [MEDIUM] AdminPaymentController.getPaymentDetails() uses getPaymentByOrderId()
  This is wrong — it looks up payment BY ORDER ID, not by payment ID.
  FIX: Add PaymentService.getPaymentById(Long id) method and use it

[116] [MEDIUM] CartController returns 200 OK for delete operations
  FIX: Return 204 NO_CONTENT for DELETE endpoints

[117] [MEDIUM] OrderController.cancelOrder returns 200 OK with no body
  FIX: Return 204 NO_CONTENT for void operations, or return the updated order

[118] [LOW] CategoryController imports CategoryRequest but never uses it
  FIX: Remove unused import

[119] [LOW] No API versioning strategy documented
  /api/v1/ is used but no migration plan exists for v2
  FIX: Document API versioning strategy

[120] [LOW] No OpenAPI/Swagger annotations on any controller
  FIX: Add @Operation, @ApiResponses annotations for auto-generated docs


═══════════════════════════════════════════════════════════════
 PHASE 4: REAL-WORLD WORKFLOW GAPS
═══════════════════════════════════════════════════════════════

These are features that exist in every production restaurant system
(Toast, Square, DoorDash Merchant) but are completely absent here:

[121] [CRITICAL] No restaurant operating hours check on order placement
  Orders can be placed at 3 AM when the restaurant is closed.
  NEED: OperatingHours entity + validation in placeOrder()

[122] [CRITICAL] No kitchen display system (KDS) integration
  Kitchen has no way to see, prioritize, or mark items as started/complete.
  NEED: Kitchen-specific API endpoints, item-level status tracking, preparation queue

[123] [HIGH] No order scheduling / pre-ordering
  Cannot place an order for future delivery/pickup time.
  NEED: scheduledFor field on Order, validation against operating hours

[124] [HIGH] No delivery tracking / ETA
  Customer cannot see where their delivery is or when it will arrive.
  NEED: Driver location updates, ETA calculation, customer-facing tracking API

[125] [HIGH] No inventory / stock management
  No way to track or deplete stock. Items can be ordered infinitely.
  NEED: StockLevel entity, auto-unavailable when stock = 0, low-stock alerts

[126] [HIGH] No receipt / invoice generation
  No printable receipt or email receipt after payment.
  NEED: PDF receipt generation, email sending service

[127] [HIGH] No email/SMS notification system
  Order confirmations, status changes, and delivery updates are WebSocket-only.
  NEED: Email notification via JavaMailSender, SMS via Twilio. Store notifications in DB

[128] [MEDIUM] No table reservation system for dine-in
  NEED: Table entity, reservation booking, time-slot availability

[129] [MEDIUM] No loyalty/rewards program
  NEED: Points accumulation, redemption, tier system

[130] [MEDIUM] No multi-language / localization support
  NEED: i18n on menu items, categories, error messages

[131] [MEDIUM] No food preparation time estimation
  NEED: Calculate based on item prep times + kitchen queue depth

[132] [LOW] No image upload/CDN for menu items
  imageUrl is a string with no upload mechanism
  NEED: File upload endpoint, S3/CDN integration

[133] [LOW] No analytics beyond basic dashboard
  NEED: Popular items, peak hours, customer acquisition, repeat rate, average ticket


═══════════════════════════════════════════════════════════════
 PHASE 5: CONFIGURATION & INFRASTRUCTURE
═══════════════════════════════════════════════════════════════

[134] [HIGH] CORS allows credentials with dynamic origins
  configuration.setAllowCredentials(true) with user-supplied origins is risky.
  FIX: Validate origins against an allowlist. Never accept wildcard + credentials

[135] [HIGH] SecurityConfig has duplicate .headers() configuration
  Two .headers() blocks — first sets frameOptions+contentTypeOptions, second overrides with CSP+HSTS+frameOptions.
  The first block is dead code.
  FIX: Merge into single .headers() block

[136] [MEDIUM] No health check / readiness probe endpoint
  FIX: Enable Spring Actuator with /health and /info endpoints

[137] [MEDIUM] No request logging / audit filter
  FIX: Add servlet filter that logs all API requests with user ID, IP, method, path, response status

[138] [MEDIUM] No database migration tool (Flyway/Liquibase)
  Schema is auto-generated by Hibernate. No versioned migrations.
  FIX: Add Flyway or Liquibase for controlled schema evolution

[139] [LOW] No Dockerfile or Docker Compose
  FIX: Add Dockerfile for the app, docker-compose.yml for app + PostgreSQL + Redis

[140] [LOW] No CI/CD pipeline configuration
  FIX: Add GitHub Actions or similar for build, test, deploy

═══════════════════════════════════════════════════════════════
 PRIORITY FIX ORDER (TOP 20)
═══════════════════════════════════════════════════════════════

 1. #7  - User.passwordHash leak (CRITICAL security)
 2. #32 - Order missing tipAmount (CRITICAL financial)
 3. #33 - Order missing taxAmount (CRITICAL financial/legal)
 4. #111- Hardcoded admin creds in source (CRITICAL security)
 5. #26 - Missing allergen info (CRITICAL legal liability)
 6. #63 - No Restaurant entity (CRITICAL architecture)
 7. #76 - No soft delete (CRITICAL data integrity)
 8. #121- No operating hours check (CRITICAL business logic)
 9. #81 - Wrong order total calculation (CRITICAL financial)
10. #90 - Controller bypasses service layer (CRITICAL architecture)
11. #34 - Order missing subtotalAmount (HIGH financial)
12. #35 - Order missing driver assignment (HIGH operations)
13. #83 - Cart race condition (HIGH concurrency)
14. #84 - No restaurant-open check (HIGH business logic)
15. #101- Missing Stripe webhook events (HIGH payment)
16. #102- No idempotency on payments (HIGH payment)
17. #106- No DataIntegrityViolation handler (HIGH error handling)
18. #92 - X-Forwarded-For spoofing (HIGH security)
19. #95 - No per-account lockout (HIGH security)
20. #5  - Refund uses wrong status enum (HIGH data integrity)
