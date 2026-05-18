# SuperMalle Restaurant System - Endpoint Testing Guide

**Date:** 2026-05-06
**Base URL:** `http://localhost:8080`
**API Version:** v1

---

## Prerequisites

Before testing endpoints, ensure:

1. ✅ Application is running on port 8080
2. ✅ PostgreSQL database is running
3. ✅ Redis cache is running
4. ✅ RabbitMQ message broker is running
5. ✅ JWT secret is configured
6. ✅ Stripe API keys are configured (for payment testing)

---

## Authentication Flow

### 1. User Registration

**Endpoint:** `POST /api/auth/register`

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "customer@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1234567890"
  }'
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "email": "customer@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "role": "CUSTOMER",
  "createdAt": "2026-05-06T00:00:00"
}
```

### 2. User Login

**Endpoint:** `POST /api/auth/login`

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "customer@example.com",
    "password": "SecurePass123!"
  }'
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "email": "customer@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "CUSTOMER"
  }
}
```

**Save the token for subsequent requests:**
```bash
export TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 3. Token Refresh

**Endpoint:** `POST /api/auth/refresh`

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{}'
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

---

## Menu Management Endpoints

### 1. List Categories

**Endpoint:** `GET /api/menu/categories`

**Request:**
```bash
curl -X GET http://localhost:8080/api/menu/categories \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Appetizers",
      "description": "Start your meal with our delicious appetizers",
      "imageUrl": "https://example.com/images/appetizers.jpg",
      "displayOrder": 1,
      "isActive": true
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 8,
    "totalPages": 1
  }
}
```

### 2. Get Category by ID

**Endpoint:** `GET /api/menu/categories/{id}`

**Request:**
```bash
curl -X GET http://localhost:8080/api/menu/categories/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "name": "Appetizers",
  "description": "Start your meal with our delicious appetizers",
  "imageUrl": "https://example.com/images/appetizers.jpg",
  "displayOrder": 1,
  "isActive": true,
  "menuItems": []
}
```

### 3. List Menu Items

**Endpoint:** `GET /api/menu/items`

**Request:**
```bash
curl -X GET "http://localhost:8080/api/menu/items?page=0&size=10&sort=name,asc" \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Caesar Salad",
      "description": "Fresh romaine lettuce with parmesan cheese and croutons",
      "price": 12.99,
      "category": {
        "id": 1,
        "name": "Appetizers"
      },
      "imageUrl": "https://example.com/images/caesar-salad.jpg",
      "isAvailable": true,
      "preparationTime": 10,
      "calories": 350
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 37,
    "totalPages": 4
  }
}
```

### 4. Get Menu Item by ID

**Endpoint:** `GET /api/menu/items/{id}`

**Request:**
```bash
curl -X GET http://localhost:8080/api/menu/items/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "name": "Caesar Salad",
  "description": "Fresh romaine lettuce with parmesan cheese and croutons",
  "price": 12.99,
  "category": {
    "id": 1,
    "name": "Appetizers"
  },
  "imageUrl": "https://example.com/images/caesar-salad.jpg",
  "isAvailable": true,
  "preparationTime": 10,
  "calories": 350,
  "ingredients": ["Romaine lettuce", "Parmesan cheese", "Croutons", "Caesar dressing"],
  "allergens": ["Dairy", "Gluten"]
}
```

---

## Cart Management Endpoints

### 1. Get Current User's Cart

**Endpoint:** `GET /api/cart`

**Request:**
```bash
curl -X GET http://localhost:8080/api/cart \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "userId": 1,
  "items": [],
  "subtotal": 0.00,
  "totalItems": 0
}
```

### 2. Add Item to Cart

**Endpoint:** `POST /api/cart/items`

**Request:**
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "menuItemId": 1,
    "quantity": 2,
    "specialInstructions": "No croutons please"
  }'
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "menuItemId": 1,
  "menuItem": {
    "id": 1,
    "name": "Caesar Salad",
    "price": 12.99
  },
  "quantity": 2,
  "specialInstructions": "No croutons please",
  "unitPrice": 12.99,
  "totalPrice": 25.98
}
```

### 3. Update Cart Item

**Endpoint:** `PUT /api/cart/items/{id}`

**Request:**
```bash
curl -X PUT http://localhost:8080/api/cart/items/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "quantity": 3
  }'
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "menuItemId": 1,
  "menuItem": {
    "id": 1,
    "name": "Caesar Salad",
    "price": 12.99
  },
  "quantity": 3,
  "specialInstructions": "No croutons please",
  "unitPrice": 12.99,
  "totalPrice": 38.97
}
```

### 4. Remove Item from Cart

**Endpoint:** `DELETE /api/cart/items/{id}`

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/cart/items/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (204 No Content)**

### 5. Clear Cart

**Endpoint:** `DELETE /api/cart`

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/cart \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (204 No Content)**

---

## Order Management Endpoints

### 1. Create Order

**Endpoint:** `POST /api/orders`

**Request:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "orderType": "DELIVERY",
    "deliveryAddress": {
      "street": "123 Main St",
      "city": "City",
      "state": "State",
      "zipCode": "12345",
      "phone": "+1234567890"
    },
    "specialInstructions": "Ring doorbell twice",
    "couponCode": "SAVE10"
  }'
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "orderNumber": "ORD-20260506-0001",
  "userId": 1,
  "orderType": "DELIVERY",
  "status": "PENDING",
  "subtotal": 50.00,
  "tax": 4.00,
  "deliveryCharge": 5.00,
  "discount": 5.00,
  "total": 54.00,
  "items": [
    {
      "id": 1,
      "menuItemId": 1,
      "menuItemName": "Caesar Salad",
      "quantity": 2,
      "unitPrice": 12.99,
      "totalPrice": 25.98
    }
  ],
  "deliveryAddress": {
    "street": "123 Main St",
    "city": "City",
    "state": "State",
    "zipCode": "12345",
    "phone": "+1234567890"
  },
  "specialInstructions": "Ring doorbell twice",
  "createdAt": "2026-05-06T00:00:00",
  "estimatedDeliveryTime": "2026-05-06T00:45:00"
}
```

### 2. List Orders

**Endpoint:** `GET /api/orders`

**Request:**
```bash
curl -X GET "http://localhost:8080/api/orders?page=0&size=10&status=PENDING" \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "orderNumber": "ORD-20260506-0001",
      "status": "PENDING",
      "total": 54.00,
      "createdAt": "2026-05-06T00:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### 3. Get Order by ID

**Endpoint:** `GET /api/orders/{id}`

**Request:**
```bash
curl -X GET http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "orderNumber": "ORD-20260506-0001",
  "userId": 1,
  "orderType": "DELIVERY",
  "status": "PENDING",
  "subtotal": 50.00,
  "tax": 4.00,
  "deliveryCharge": 5.00,
  "discount": 5.00,
  "total": 54.00,
  "items": [...],
  "deliveryAddress": {...},
  "specialInstructions": "Ring doorbell twice",
  "createdAt": "2026-05-06T00:00:00",
  "estimatedDeliveryTime": "2026-05-06T00:45:00",
  "statusHistory": [
    {
      "status": "PENDING",
      "timestamp": "2026-05-06T00:00:00",
      "notes": "Order created"
    }
  ]
}
```

### 4. Cancel Order

**Endpoint:** `PUT /api/orders/{id}/cancel`

**Request:**
```bash
curl -X PUT http://localhost:8080/api/orders/1/cancel \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "reason": "Changed my mind"
  }'
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "orderNumber": "ORD-20260506-0001",
  "status": "CANCELLED",
  "cancellationReason": "Changed my mind",
  "cancelledAt": "2026-05-06T00:05:00"
}
```

---

## Payment Endpoints

### 1. Create Payment Intent

**Endpoint:** `POST /api/payments/create-intent`

**Request:**
```bash
curl -X POST http://localhost:8080/api/payments/create-intent \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "orderId": 1,
    "amount": 54.00,
    "currency": "USD"
  }'
```

**Expected Response (200 OK):**
```json
{
  "paymentIntentId": "pi_3MtwBw2eZvKYlo2C1g5c5c5c",
  "clientSecret": "pi_3MtwBw2eZvKYlo2C1g5c5c5c_secret_abc123",
  "amount": 54.00,
  "currency": "USD",
  "status": "REQUIRES_PAYMENT_METHOD"
}
```

### 2. Confirm Payment

**Endpoint:** `POST /api/payments/confirm`

**Request:**
```bash
curl -X POST http://localhost:8080/api/payments/confirm \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "paymentIntentId": "pi_3MtwBw2eZvKYlo2C1g5c5c5c",
    "paymentMethodId": "pm_card_visa"
  }'
```

**Expected Response (200 OK):**
```json
{
  "paymentId": 1,
  "paymentIntentId": "pi_3MtwBw2eZvKYlo2C1g5c5c5c",
  "orderId": 1,
  "amount": 54.00,
  "currency": "USD",
  "status": "COMPLETED",
  "paidAt": "2026-05-06T00:10:00"
}
```

### 3. Get Payment Details

**Endpoint:** `GET /api/payments/{id}`

**Request:**
```bash
curl -X GET http://localhost:8080/api/payments/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "paymentIntentId": "pi_3MtwBw2eZvKYlo2C1g5c5c5c",
  "orderId": 1,
  "orderNumber": "ORD-20260506-0001",
  "amount": 54.00,
  "currency": "USD",
  "status": "COMPLETED",
  "paidAt": "2026-05-06T00:10:00",
  "refundedAmount": 0.00
}
```

---

## Coupon Management Endpoints

### 1. List Coupons

**Endpoint:** `GET /api/coupons`

**Request:**
```bash
curl -X GET "http://localhost:8080/api/coupons?page=0&size=10&isActive=true" \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "code": "SAVE10",
      "discountType": "PERCENTAGE",
      "value": 10.00,
      "minOrderAmount": 50.00,
      "maxDiscountAmount": 20.00,
      "usageLimit": 100,
      "usageCount": 25,
      "isActive": true,
      "expiresAt": "2026-12-31T23:59:59"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 5,
    "totalPages": 1
  }
}
```

### 2. Get Coupon by Code

**Endpoint:** `GET /api/coupons/{code}`

**Request:**
```bash
curl -X GET http://localhost:8080/api/coupons/SAVE10 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "code": "SAVE10",
  "discountType": "PERCENTAGE",
  "value": 10.00,
  "minOrderAmount": 50.00,
  "maxDiscountAmount": 20.00,
  "usageLimit": 100,
  "usageCount": 25,
  "isActive": true,
  "expiresAt": "2026-12-31T23:59:59",
  "isApplicable": true
}
```

---

## Health Check Endpoints

### 1. General Health Check

**Endpoint:** `GET /actuator/health`

**Request:**
```bash
curl -X GET http://localhost:8080/actuator/health
```

**Expected Response (200 OK):**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760,
        "path": "/home/kai/Downloads/superMalleMevan/."
      }
    },
    "ping": {
      "status": "UP"
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    },
    "rabbitmq": {
      "status": "UP",
      "details": {
        "host": "localhost",
        "port": 5672
      }
    }
  }
}
```

### 2. Liveness Probe

**Endpoint:** `GET /actuator/health/liveness`

**Request:**
```bash
curl -X GET http://localhost:8080/actuator/health/liveness
```

**Expected Response (200 OK):**
```json
{
  "status": "UP",
  "groups": ["liveness"]
}
```

### 3. Readiness Probe

**Endpoint:** `GET /actuator/health/readiness`

**Request:**
```bash
curl -X GET http://localhost:8080/actuator/health/readiness
```

**Expected Response (200 OK):**
```json
{
  "status": "UP",
  "groups": ["readiness"]
}
```

---

## Error Response Examples

### 400 Bad Request
```json
{
  "timestamp": "2026-05-06T00:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    },
    {
      "field": "password",
      "message": "Password must be at least 8 characters"
    }
  ],
  "path": "/api/auth/register"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2026-05-06T00:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "path": "/api/orders"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2026-05-06T00:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. You don't have permission to access this resource",
  "path": "/api/admin/dashboard"
}
```

### 404 Not Found
```json
{
  "timestamp": "2026-05-06T00:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with id: 999",
  "path": "/api/orders/999"
}
```

### 409 Conflict
```json
{
  "timestamp": "2026-05-06T00:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Email already registered",
  "path": "/api/auth/register"
}
```

### 429 Too Many Requests
```json
{
  "timestamp": "2026-05-06T00:00:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again later",
  "path": "/api/orders"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2026-05-06T00:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/orders"
}
```

---

## Testing Checklist

### Authentication
- [ ] User registration with valid data
- [ ] User registration with invalid data (email format, password strength)
- [ ] User login with correct credentials
- [ ] User login with incorrect credentials
- [ ] Token refresh
- [ ] Token expiration handling
- [ ] Logout functionality

### Menu Management
- [ ] List all categories
- [ ] Get category by ID
- [ ] List menu items with pagination
- [ ] List menu items with filters
- [ ] Get menu item by ID
- [ ] Search menu items
- [ ] Filter by category

### Cart Management
- [ ] Get empty cart
- [ ] Add item to cart
- [ ] Add multiple items to cart
- [ ] Update cart item quantity
- [ ] Update cart item special instructions
- [ ] Remove item from cart
- [ ] Clear entire cart
- [ ] Calculate cart totals correctly

### Order Management
- [ ] Create order from cart
- [ ] Create order with delivery address
- [ ] Create order with pickup
- [ ] Apply coupon to order
- [ ] List user's orders
- [ ] Filter orders by status
- [ ] Get order details
- [ ] Cancel order (before preparation)
- [ ] Cancel order (during preparation - should fail)
- [ ] Track order status updates

### Payment Processing
- [ ] Create payment intent
- [ ] Confirm payment with valid card
- [ ] Confirm payment with invalid card
- [ ] Handle payment failures
- [ ] Process refunds
- [ ] Get payment details
- [ ] Idempotency handling (duplicate payment requests)

### Coupon Management
- [ ] List active coupons
- [ ] Get coupon by code
- [ ] Apply percentage discount
- [ ] Apply fixed amount discount
- [ ] Validate coupon expiration
- [ ] Validate coupon usage limits
- [ ] Validate minimum order amount
- [ ] Validate maximum discount amount

### Error Handling
- [ ] 400 Bad Request responses
- [ ] 401 Unauthorized responses
- [ ] 403 Forbidden responses
- [ ] 404 Not Found responses
- [ ] 409 Conflict responses
- [ ] 429 Rate Limit responses
- [ ] 500 Internal Server Error responses

### Security
- [ ] JWT token validation
- [ ] Role-based access control
- [ ] Rate limiting enforcement
- [ ] Input validation
- [ ] SQL injection prevention
- [ ] XSS protection

### Performance
- [ ] Response time under 200ms for simple queries
- [ ] Response time under 500ms for complex queries
- [ ] Handle concurrent requests
- [ ] Database connection pool efficiency
- [ ] Cache hit rates

---

## Automated Testing Script

Create a test script `test-endpoints.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"
TOKEN=""

echo "=== SuperMalle API Testing ==="
echo ""

# Test 1: Register User
echo "Test 1: Register User"
REGISTER_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "TestPass123!",
    "firstName": "Test",
    "lastName": "User",
    "phone": "+1234567890"
  }')
echo "Response: $REGISTER_RESPONSE"
echo ""

# Test 2: Login
echo "Test 2: Login"
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "TestPass123!"
  }')
echo "Response: $LOGIN_RESPONSE"
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')
echo "Token: $TOKEN"
echo ""

# Test 3: Get Categories
echo "Test 3: Get Categories"
CATEGORIES_RESPONSE=$(curl -s -X GET $BASE_URL/api/menu/categories \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $CATEGORIES_RESPONSE"
echo ""

# Test 4: Get Menu Items
echo "Test 4: Get Menu Items"
MENU_ITEMS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/menu/items?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $MENU_ITEMS_RESPONSE"
echo ""

# Test 5: Get Cart
echo "Test 5: Get Cart"
CART_RESPONSE=$(curl -s -X GET $BASE_URL/api/cart \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $CART_RESPONSE"
echo ""

# Test 6: Health Check
echo "Test 6: Health Check"
HEALTH_RESPONSE=$(curl -s -X GET $BASE_URL/actuator/health)
echo "Response: $HEALTH_RESPONSE"
echo ""

echo "=== Testing Complete ==="
```

Run the script:
```bash
chmod +x test-endpoints.sh
./test-endpoints.sh
```

---

**Document Version:** 1.0
**Last Updated:** 2026-05-06
**Maintained By:** SuperMalle Development Team
