# SuperMalle API Reference Guide

**Base URL:** `http://localhost:8080/api/v1`
**Authentication:** JWT Bearer Token (`Authorization: Bearer <token>`)

---

## Contents

- [Auth](#auth)
- [Menu & Categories](#menu--categories)
- [Cart](#cart)
- [Orders](#orders)
- [Order Modifications](#order-modifications)
- [Payments](#payments)
- [Coupons](#coupons)
- [Reviews](#reviews)
- [Loyalty](#loyalty)
- [Inventory](#inventory)
- [Admin Dashboard](#admin-dashboard)
- [Admin Orders](#admin-orders)
- [Admin Menu](#admin-menu)
- [Admin Categories](#admin-categories)
- [Admin Coupons](#admin-coupons)
- [Admin Payments](#admin-payments)
- [Admin Users](#admin-users)
- [Admin Reviews](#admin-reviews)
- [Admin Settings](#admin-settings)
- [Admin Operating Hours](#admin-operating-hours)
- [Admin Announcements](#admin-announcements)
- [WebSocket Topics](#websocket-topics)
- [Error Responses](#error-responses)
- [Rate Limiting](#rate-limiting)
- [Testing Credentials](#testing-credentials)

---

## Auth

### Register
```
POST /auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "phone": "+1-555-123-4567"
}
```

### Login
```
POST /auth/login
Content-Type: application/json

{
  "email": "admin@supermalle.com",
  "password": "Admin@2026!"
}
```
**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "type": "Bearer",
  "id": 2,
  "name": "Admin",
  "email": "admin@supermalle.com",
  "role": "ADMIN"
}
```

### Refresh Token
```
POST /auth/refresh
Authorization: Bearer <token>
Content-Type: application/json

{ "refreshToken": "<refresh_token>" }
```

### Change Password
```
POST /auth/change-password
Authorization: Bearer <token>
Content-Type: application/json

{
  "currentPassword": "oldPass123!",
  "newPassword": "newPass456!"
}
```

### Forgot Password
```
POST /auth/forgot-password
Content-Type: application/json

{ "email": "user@example.com" }
```

### Reset Password
```
POST /auth/reset-password
Content-Type: application/json

{
  "token": "<reset_token>",
  "newPassword": "newPass456!"
}
```

---

## Menu & Categories

### Get Active Categories
```
GET /categories
```

### Get Menu (grouped by category)
```
GET /menu
```

### Get Single Menu Item
```
GET /menu/{id}
```

### Search Menu Items
```
GET /menu/search?q=pizza&categoryId=1&page=0&size=10
```

### Get Items by Category
```
GET /menu/category/{categoryId}?page=0&size=10
```

---

## Cart

### Get Cart
```
GET /cart
Authorization: Bearer <token>
```

### Add to Cart
```
POST /cart/add
Authorization: Bearer <token>
Content-Type: application/json

{
  "menuItemId": 1,
  "quantity": 2,
  "specialInstructions": "No onions",
  "selectedOptions": [
    { "optionGroupId": 1, "optionId": 3 }
  ]
}
```

### Update Cart Item
```
PUT /cart/update/{cartItemId}
Authorization: Bearer <token>
Content-Type: application/json

{ "quantity": 3 }
```

### Remove from Cart
```
DELETE /cart/remove/{cartItemId}
Authorization: Bearer <token>
```

### Clear Cart
```
DELETE /cart/clear
Authorization: Bearer <token>
```

---

## Orders

### Place Order
```
POST /orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "orderType": "DELIVERY",
  "deliveryAddress": "123 Main St, City",
  "specialInstructions": "Ring doorbell",
  "paymentMethodId": "pm_123",
  "couponCode": "SAVE10"
}
```

### Get My Orders
```
GET /orders?page=0&size=10
Authorization: Bearer <token>
```

### Get Order Detail
```
GET /orders/{id}
Authorization: Bearer <token>
```

### Cancel Order
```
POST /orders/{id}/cancel
Authorization: Bearer <token>
```

---

## Order Modifications

### Request Modification
```
POST /order-modifications
Authorization: Bearer <token>
Content-Type: application/json

{
  "orderId": 1,
  "modificationType": "CHANGE_QUANTITY",
  "requestedChange": "Increase quantity by 2",
  "reason": "Customer requested more"
}
```

**Types:** `ADD_ITEM`, `REMOVE_ITEM`, `UPDATE_QUANTITY`, `UPDATE_ADDRESS`, `CANCEL_ITEM`

### Get My Modifications
```
GET /order-modifications/my
Authorization: Bearer <token>
```

### Get Order Modifications
```
GET /order-modifications/order/{orderId}
Authorization: Bearer <token>
```

### Get Pending Modifications (Admin)
```
GET /order-modifications/pending
Authorization: Bearer <token>
```

### Approve Modification (Admin)
```
POST /order-modifications/approve
Authorization: Bearer <token>
Content-Type: application/json

{
  "modificationId": 1,
  "adminNotes": "Approved as requested"
}
```

### Reject Modification (Admin)
```
POST /order-modifications/reject
Authorization: Bearer <token>
Content-Type: application/json

{
  "modificationId": 1,
  "rejectionReason": "Cannot modify after preparation started"
}
```

### Get Pending Count (Admin)
```
GET /order-modifications/stats/pending-count
Authorization: Bearer <token>
```

---

## Payments

### Create Payment Intent
```
POST /payments/create-intent
Authorization: Bearer <token>
Content-Type: application/json

{
  "orderId": 1,
  "currency": "usd"
}
```

### Confirm Payment
```
POST /payments/confirm
Authorization: Bearer <token>
Content-Type: application/json

{
  "paymentIntentId": "pi_123",
  "paymentMethodId": "pm_123"
}
```

### Stripe Webhook
```
POST /payments/webhook
Content-Type: application/json
Stripe-Signature: <webhook_signature>
```

### Get Payment by ID
```
GET /payments/{id}
Authorization: Bearer <token>
```

### Request Refund
```
POST /payments/{id}/refund
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 25.00,
  "reason": "Customer requested refund"
}
```

---

## Coupons

### Validate Coupon
```
POST /coupons/validate
Authorization: Bearer <token>
Content-Type: application/json

{
  "code": "SAVE10",
  "orderAmount": 50.00
}
```

---

## Reviews

### Get Item Reviews
```
GET /reviews/item/{menuItemId}
```

### Submit Review
```
POST /reviews
Authorization: Bearer <token>
Content-Type: application/json

{
  "menuItemId": 1,
  "rating": 5,
  "comment": "Amazing food!"
}
```

---

## Loyalty

### Get Active Program (Public)
```
GET /loyalty/program
```

### Get Leaderboard (Public)
```
GET /loyalty/leaderboard?limit=10
```

### Get My Loyalty Info
```
GET /loyalty/me
Authorization: Bearer <token>
```

### Get Transaction History
```
GET /loyalty/me/transactions
Authorization: Bearer <token>
```

### Redeem Points
```
POST /loyalty/me/redeem
Authorization: Bearer <token>
Content-Type: application/json

{
  "pointsToRedeem": 500,
  "orderId": 1
}
```

### Enroll in Loyalty Program
```
POST /loyalty/enroll
Authorization: Bearer <token>
```

### Apply Referral Code
```
POST /loyalty/apply-referral?referralCode=88811490
Authorization: Bearer <token>
```

### Get All Programs (Admin)
```
GET /loyalty/programs
Authorization: Bearer <token>
```

### Create Program (Admin)
```
POST /loyalty/programs
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "SuperMalle Rewards",
  "pointsPerDollar": 10,
  "redemptionRate": 100,
  "minPointsToRedeem": 500,
  "maxPointsPerOrder": 1000,
  "welcomeBonusPoints": 100,
  "referralBonusPoints": 500
}
```

### Update Program (Admin)
```
PUT /loyalty/programs/{id}
Authorization: Bearer <token>
```

### Delete Program (Admin)
```
DELETE /loyalty/programs/{id}
Authorization: Bearer <token>
```

### Get User Loyalty (Admin)
```
GET /loyalty/users/{userId}
Authorization: Bearer <token>
```

### Enroll User (Admin)
```
POST /loyalty/users/{userId}/enroll
Authorization: Bearer <token>
```

### Award Points for Order (Admin)
```
POST /loyalty/orders/{orderId}/award-points
Authorization: Bearer <token>
```

### Get User Transactions (Admin)
```
GET /loyalty/users/{userId}/transactions
Authorization: Bearer <token>
```

---

## Inventory

### Get All Inventory
```
GET /inventory
Authorization: Bearer <token>
```

### Get Single Item
```
GET /inventory/{id}
Authorization: Bearer <token>
```

### Update Inventory
```
PUT /inventory/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "menuItemId": 1,
  "quantity": 150,
  "reorderLevel": 30,
  "unit": "pieces",
  "costPerUnit": 6.0,
  "supplierName": "Supplier Co",
  "supplierContact": "supplier@example.com"
}
```

### Restock
```
POST /inventory/{id}/restock
Authorization: Bearer <token>
Content-Type: application/json

{
  "quantityToAdd": 50,
  "supplierName": "Restock Supplier"
}
```

### Get Low Stock Items
```
GET /inventory/low-stock
Authorization: Bearer <token>
```

---

## Admin Dashboard

```
GET /admin/dashboard
Authorization: Bearer <token>
```
Returns: total revenue, order count, customer count, sales chart data, top items, recent orders.

---

## Admin Orders

### List All Orders
```
GET /admin/orders?page=0&size=20&status=PENDING&from=2026-01-01&to=2026-12-31
Authorization: Bearer <token>
```

### Update Order Status
```
PUT /admin/orders/{id}/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "PREPARING"
}
```

### Update ETA
```
PUT /admin/orders/{id}/eta
Authorization: Bearer <token>
Content-Type: application/json

{
  "etaMinutes": 25
}
```

---

## Admin Menu

### List All Items
```
GET /admin/menu?page=0&size=20&categoryId=1&search=pizza
Authorization: Bearer <token>
```

### Create Item
```
POST /admin/menu
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Margherita Pizza",
  "description": "Classic tomato and mozzarella",
  "price": 12.99,
  "categoryId": 1,
  "isAvailable": true,
  "preparationTimeMinutes": 15,
  "dietaryTags": ["VEGETARIAN"],
  "allergens": ["GLUTEN", "DAIRY"],
  "imageUrl": "https://example.com/pizza.jpg",
  "optionGroups": [
    {
      "name": "Size",
      "isRequired": true,
      "maxSelections": 1,
      "options": [
        { "name": "Small", "priceAdjustment": 0 },
        { "name": "Large", "priceAdjustment": 4.00 }
      ]
    }
  ]
}
```

### Update Item
```
PUT /admin/menu/{id}
Authorization: Bearer <token>
```

### Delete Item
```
DELETE /admin/menu/{id}
Authorization: Bearer <token>
```

---

## Admin Categories

### List All Categories
```
GET /admin/categories
Authorization: Bearer <token>
```

### Create Category
```
POST /admin/categories
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Beverages",
  "imageUrl": "https://example.com/drinks.jpg",
  "sortOrder": 4
}
```

### Update Category
```
PUT /admin/categories/{id}
Authorization: Bearer <token>
```

### Delete Category (soft)
```
DELETE /admin/categories/{id}
Authorization: Bearer <token>
```

---

## Admin Coupons

### List All Coupons
```
GET /admin/coupons
Authorization: Bearer <token>
```

### Create Coupon
```
POST /admin/coupons
Authorization: Bearer <token>
Content-Type: application/json

{
  "code": "SUMMER20",
  "type": "PERCENTAGE",
  "value": 20.0,
  "maxUsageCount": 100,
  "maxUsagePerUser": 1,
  "minOrderAmount": 30.0,
  "maxDiscountAmount": 50.0,
  "startDate": "2026-06-01T00:00:00",
  "endDate": "2026-08-31T23:59:59",
  "isActive": true
}
```

### Update Coupon
```
PUT /admin/coupons/{id}
Authorization: Bearer <token>
```

### Delete Coupon
```
DELETE /admin/coupons/{id}
Authorization: Bearer <token>
```

---

## Admin Payments

### List All Payments
```
GET /admin/payments?page=0&size=20&status=COMPLETED&from=2026-01-01&to=2026-12-31
Authorization: Bearer <token>
```

### Process Refund
```
POST /admin/payments/{id}/refund
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 25.00,
  "reason": "Item out of stock"
}
```

---

## Admin Users

### List All Users
```
GET /admin/users?page=0&size=20&role=CUSTOMER&search=john
Authorization: Bearer <token>
```

### Create User
```
POST /admin/users
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "New Staff",
  "email": "staff@supermalle.com",
  "role": "STAFF",
  "phone": "+1-555-999-9999"
}
```
Response includes generated password and sends welcome email.

### Update User
```
PUT /admin/users/{id}
Authorization: Bearer <token>
```

### Reset Password
```
POST /admin/users/{id}/reset-password
Authorization: Bearer <token>
```

---

## Admin Reviews

### List All Reviews
```
GET /admin/reviews?status=PENDING
Authorization: Bearer <token>
```

### Approve Review
```
PUT /admin/reviews/{id}/approve
Authorization: Bearer <token>
```

### Reject Review
```
PUT /admin/reviews/{id}/reject
Authorization: Bearer <token>
```

---

## Admin Settings

### Get Settings
```
GET /admin/settings
Authorization: Bearer <token>
```

### Update Settings
```
PUT /admin/settings
Authorization: Bearer <token>
Content-Type: application/json

{
  "restaurantName": "SuperMalle Restaurant",
  "taxRate": 0.08,
  "deliveryCharge": 5.00,
  "phone": "+1-555-123-4567",
  "address": "123 Main St, City, State 12345"
}
```

---

## Admin Operating Hours

### Get Hours
```
GET /admin/hours
Authorization: Bearer <token>
```

### Update Hours
```
PUT /admin/hours
Authorization: Bearer <token>
Content-Type: application/json

{
  "hours": [
    { "dayOfWeek": "MONDAY", "openTime": "09:00", "closeTime": "22:00", "isClosed": false }
  ]
}
```

---

## Admin Announcements

### Send Announcement
```
POST /admin/announce
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "New Menu Items!",
  "message": "Check out our new summer specials!"
}
```

---

## WebSocket Topics

Connect to `ws://localhost:8080/ws` via STOMP.

| Topic | Description |
|-------|-------------|
| `/topic/orders` | Order status updates |
| `/topic/orders/{orderNumber}` | Specific order updates |
| `/topic/admin/orders` | Admin order notifications |
| `/topic/admin/inventory` | Inventory changes |
| `/topic/admin/inventory/alerts` | Low stock alerts |
| `/topic/admin/order-modifications` | Modification requests |
| `/topic/user/{userId}/loyalty` | User loyalty updates |

---

## Error Responses

### 400 Bad Request
```json
{ "status": 400, "error": "Bad Request", "message": "Validation failed" }
```

### 401 Unauthorized
```json
{ "status": 401, "error": "Unauthorized", "message": "Invalid token" }
```

### 403 Forbidden
```json
{ "status": 403, "error": "Forbidden", "message": "Access denied" }
```

### 404 Not Found
```json
{ "status": 404, "error": "Not Found", "message": "Order not found with id: 1" }
```

### 429 Too Many Requests
```json
{ "status": 429, "error": "Too Many Requests", "message": "Rate limit exceeded" }
```

### 500 Internal Server Error
```json
{ "status": 500, "error": "Internal Server Error", "message": "An unexpected error occurred" }
```

---

## Loyalty Tier System

| Tier | Points Range | Multiplier | Benefits |
|------|-------------|------------|----------|
| Bronze | 0–999 | 1.0x | Standard benefits |
| Silver | 1,000–4,999 | 1.1x | 10% bonus points |
| Gold | 5,000–9,999 | 1.25x | 25% bonus points |
| Platinum | 10,000+ | 1.5x | 50% bonus points |

**Points:** 10 per $1 spent. Redemption: 100 points = $1. Welcome bonus: 100. Referral bonus: 500.

---

## Rate Limiting

| Role | Limit |
|------|-------|
| Default | 100 requests/minute |
| Admin | 1000 requests/minute |
| Staff | 500 requests/minute |
| Customer | 100 requests/minute |

Disabled in `dev` profile.

---

## Testing Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@supermalle.com | Admin@2026! |
| Customer | test@example.com | test123 |
