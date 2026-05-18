# SuperMalle API Reference Guide

**Base URL:** `http://localhost:8080`
**Authentication:** JWT Bearer Token

---

## Authentication

### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@supermalle.com",
  "password": "Admin@2026!"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 2,
  "email": "admin@supermalle.com",
  "role": "ADMIN"
}
```

---

## Inventory Management

### Get All Inventory
```http
GET /api/v1/inventory
Authorization: Bearer <token>
```

### Get Single Inventory Item
```http
GET /api/v1/inventory/{id}
Authorization: Bearer <token>
```

### Update Inventory
```http
PUT /api/v1/inventory/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "menuItemId": 1,
  "quantity": 150,
  "reorderLevel": 30,
  "maxQuantity": 300,
  "unit": "pieces",
  "costPerUnit": 6.0,
  "supplierName": "Updated Supplier",
  "supplierContact": "updated@example.com"
}
```

### Restock Inventory
```http
POST /api/v1/inventory/{id}/restock
Authorization: Bearer <token>
Content-Type: application/json

{
  "quantityToAdd": 50,
  "supplierName": "Restock Supplier",
  "supplierContact": "restock@example.com"
}
```

### Get Low Stock Items
```http
GET /api/v1/inventory/low-stock
Authorization: Bearer <token>
```

---

## Loyalty Program

### Get Active Program (Public)
```http
GET /api/v1/loyalty/program
```

**Response:**
```json
{
  "id": 1,
  "name": "SuperMalle Rewards",
  "description": "Earn points with every order and redeem for discounts!",
  "pointsPerDollar": 10,
  "redemptionRate": 100,
  "minPointsToRedeem": 500,
  "maxPointsPerOrder": 1000,
  "welcomeBonusPoints": 100,
  "referralBonusPoints": 500,
  "isActive": true
}
```

### Get Leaderboard (Public)
```http
GET /api/v1/loyalty/leaderboard?limit=10
```

### Get My Loyalty Info
```http
GET /api/v1/loyalty/me
Authorization: Bearer <token>
```

**Response:**
```json
{
  "id": 1,
  "userId": 2,
  "userName": "Admin",
  "userEmail": "admin@supermalle.com",
  "loyaltyProgramId": 1,
  "loyaltyProgramName": "SuperMalle Rewards",
  "totalPoints": 100,
  "availablePoints": 100,
  "redeemedPoints": 0,
  "tierLevel": "BRONZE",
  "lifetimePoints": 100,
  "totalOrders": 0,
  "totalSpent": 0.0,
  "referralCode": "88811490",
  "referralCount": 0,
  "isActive": true,
  "tierMultiplier": 1.0,
  "tierBenefits": "Standard benefits",
  "pointsToNextTier": 900,
  "nextTier": "SILVER",
  "discountValue": 0.0
}
```

### Redeem Points
```http
POST /api/v1/loyalty/me/redeem
Authorization: Bearer <token>
Content-Type: application/json

{
  "pointsToRedeem": 500,
  "orderId": 1
}
```

### Get Transaction History
```http
GET /api/v1/loyalty/me/transactions
Authorization: Bearer <token>
```

### Enroll in Loyalty Program
```http
POST /api/v1/loyalty/enroll
Authorization: Bearer <token>
```

### Apply Referral Code
```http
POST /api/v1/loyalty/apply-referral?referralCode=88811490
Authorization: Bearer <token>
```

---

## Order Modifications

### Request Modification
```http
POST /api/v1/order-modifications
Authorization: Bearer <token>
Content-Type: application/json

{
  "orderId": 1,
  "modificationType": "CHANGE_QUANTITY",
  "requestedChange": "Increase quantity by 2",
  "reason": "Customer requested more"
}
```

**Modification Types:**
- `ADD_ITEM` - Add items to order
- `REMOVE_ITEM` - Remove items from order
- `UPDATE_QUANTITY` - Update item quantities
- `UPDATE_ADDRESS` - Update delivery address
- `CANCEL_ITEM` - Cancel specific items

### Get My Modifications
```http
GET /api/v1/order-modifications/my
Authorization: Bearer <token>
```

### Get Order Modifications
```http
GET /api/v1/order-modifications/order/{orderId}
Authorization: Bearer <token>
```

### Get Pending Modifications (Admin)
```http
GET /api/v1/order-modifications/pending
Authorization: Bearer <token>
```

### Approve Modification (Admin)
```http
POST /api/v1/order-modifications/approve
Authorization: Bearer <token>
Content-Type: application/json

{
  "modificationId": 1,
  "adminNotes": "Approved as requested"
}
```

### Reject Modification (Admin)
```http
POST /api/v1/order-modifications/reject
Authorization: Bearer <token>
Content-Type: application/json

{
  "modificationId": 1,
  "rejectionReason": "Cannot modify after preparation started"
}
```

### Get Pending Count (Admin)
```http
GET /api/v1/order-modifications/stats/pending-count
Authorization: Bearer <token>
```

---

## Admin Endpoints

### Get All Loyalty Programs
```http
GET /api/v1/loyalty/programs
Authorization: Bearer <token>
```

### Create Loyalty Program
```http
POST /api/v1/loyalty/programs
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "SuperMalle Rewards",
  "description": "Earn points with every order!",
  "pointsPerDollar": 10,
  "redemptionRate": 100,
  "minPointsToRedeem": 500,
  "maxPointsPerOrder": 1000,
  "welcomeBonusPoints": 100,
  "referralBonusPoints": 500
}
```

### Update Loyalty Program
```http
PUT /api/v1/loyalty/programs/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Updated Program Name",
  "pointsPerDollar": 15
}
```

### Delete Loyalty Program
```http
DELETE /api/v1/loyalty/programs/{id}
Authorization: Bearer <token>
```

### Get User Loyalty (Admin)
```http
GET /api/v1/loyalty/users/{userId}
Authorization: Bearer <token>
```

### Get User Transaction History (Admin)
```http
GET /api/v1/loyalty/users/{userId}/transactions
Authorization: Bearer <token>
```

### Enroll User in Loyalty (Admin)
```http
POST /api/v1/loyalty/users/{userId}/enroll
Authorization: Bearer <token>
```

### Award Points for Order (Admin)
```http
POST /api/v1/loyalty/orders/{orderId}/award-points
Authorization: Bearer <token>
```

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2026-05-05T20:41:24.470059106",
  "status": 400,
  "error": "Bad Request",
  "message": "Inventory already exists for this menu item",
  "path": "/api/v1/inventory"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2026-05-05T20:41:24.470059106",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid token"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2026-05-05T20:41:24.470059106",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied"
}
```

### 404 Not Found
```json
{
  "timestamp": "2026-05-05T20:41:24.470059106",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with id: 1"
}
```

---

## WebSocket Topics

### Inventory Updates
```
/topic/admin/inventory
/topic/admin/inventory/alerts
```

### Order Modifications
```
/topic/admin/order-modifications
/topic/orders/{orderNumber}/modifications
```

### Loyalty Updates
```
/topic/user/{userId}/loyalty
```

---

## Tier System

| Tier | Points Range | Multiplier | Benefits |
|------|-------------|------------|----------|
| Bronze | 0-999 | 1.0x | Standard benefits |
| Silver | 1,000-4,999 | 1.1x | 10% bonus points |
| Gold | 5,000-9,999 | 1.25x | 25% bonus points |
| Platinum | 10,000+ | 1.5x | 50% bonus points |

---

## Points System

- **Earning:** 10 points per $1 spent
- **Redemption:** 100 points = $1 discount
- **Welcome Bonus:** 100 points on enrollment
- **Referral Bonus:** 500 points for both referrer and referee
- **Minimum Redemption:** 500 points ($5 discount)
- **Maximum Per Order:** 1000 points ($10 discount)

---

## Testing Credentials

### Admin User
- Email: admin@supermalle.com
- Password: Admin@2026!
- Role: ADMIN

### Test Customer
- Email: test@example.com
- Password: test123
- Role: CUSTOMER

---

## Common Use Cases

### 1. Customer Places Order and Earns Points
1. Customer places order through existing order API
2. Admin calls `POST /api/v1/loyalty/orders/{orderId}/award-points`
3. Customer receives points automatically
4. Customer can check points via `GET /api/v1/loyalty/me`

### 2. Customer Requests Order Modification
1. Customer calls `POST /api/v1/order-modifications`
2. Admin sees request via `GET /api/v1/order-modifications/pending`
3. Admin approves or rejects via respective endpoints
4. Customer receives notification via WebSocket

### 3. Customer Redeems Points
1. Customer checks available points via `GET /api/v1/loyalty/me`
2. Customer calls `POST /api/v1/loyalty/me/redeem`
3. Points are deducted and discount applied
4. Transaction recorded in history

### 4. Admin Monitors Inventory
1. Admin calls `GET /api/v1/inventory/low-stock`
2. Admin sees items needing restock
3. Admin calls `POST /api/v1/inventory/{id}/restock`
4. Inventory updated and notification sent

---

## Rate Limiting

Currently not implemented. Consider adding for production:
- 100 requests per minute per user
- 1000 requests per minute per IP

---

## CORS Configuration

Allowed origins (configurable in application.yml):
- http://localhost:3000 (default for React dev)
- https://yourdomain.com (production)

---

## Notes

- All datetime fields are in ISO 8601 format
- All monetary values are in USD
- All IDs are Long integers
- JWT tokens expire in 24 hours (configurable)
- WebSocket connections require authentication

---

## Support

For issues or questions:
1. Check FINAL_STATUS_REPORT.md for system status
2. Review IMPLEMENTATION_SUMMARY.md for detailed documentation
3. Verify JWT token is valid and not expired
4. Ensure user has correct role for requested endpoint

**Status:** ✅ API READY FOR INTEGRATION
