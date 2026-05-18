# API Documentation Examples
## SuperMalle Restaurant System

This document provides example requests and responses for all API endpoints.

---

## Authentication Endpoints

### Register User

**POST** `/api/v1/auth/register`

**Request:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890"
}
```

**Response:**
```json
{
  "timestamp": "2026-05-05T10:00:00Z",
  "status": 201,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "role": "CUSTOMER",
    "createdAt": "2026-05-05T10:00:00Z"
  }
}
```

### Login

**POST** `/api/v1/auth/login`

**Request:**
```json
{
  "username": "john_doe",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "timestamp": "2026-05-05T10:00:00Z",
  "status": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "role": "CUSTOMER"
    }
  }
}
```

---

## Menu Endpoints

### Get All Menu Items

**GET** `/api/v1/menu`

**Response:**
```json
{
  "timestamp": "2026-05-05T10:00:00Z",
  "status": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Classic Burger",
        "description": "Juicy beef patty with fresh vegetables",
        "price": 12.99,
        "category": {
          "id": 1,
          "name": "Burgers",
          "description": "Delicious burger options"
        },
        "available": true,
        "imageUrl": "https://example.com/images/burger.jpg",
        "preparationTime": 15
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 37,
    "totalPages": 2
  }
}
```

### Get Menu Item by ID

**GET** `/api/v1/menu/{id}`

**Response:**
```json
{
  "timestamp": "2026-05-05T10:00:00Z",
  "status": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Classic Burger",
    "description": "Juicy beef patty with fresh vegetables",
    "price": 12.99,
    "category": {
      "id": 1,
      "name": "Burgers",
      "description": "Delicious burger options"
    },
    "available": true,
    "imageUrl": "https://example.com/images/burger.jpg",
    "preparationTime": 15,
    "ingredients": ["Beef patty", "Lettuce", "Tomato", "Onion", "Cheese"],
    "allergens": ["Gluten", "Dairy"]
  }
}
```

---

## Order Endpoints

### Create Order

**POST** `/api/v1/orders`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Request:**
```json
{
  "items": [
    {
      "menuItemId": 1,
      "quantity": 2,
      "specialInstructions": "No onions"
    },
    {
      "menuItemId": 5,
      "quantity": 1
    }
  ],
  "deliveryAddress": "123 Main St, City, State 12345",
  "deliveryInstructions": "Ring doorbell twice",
  "paymentMethod": "CREDIT_CARD"
}
```

**Response:**
```json
{
  "timestamp": "2026-05-05T10:00:00Z",
  "status": 201,
  "message": "Order created successfully",
  "data": {
    "id": 1001,
    "userId": 1,
    "status": "PENDING",
    "items": [
      {
        "id": 1,
        "menuItemId": 1,
        "name": "Classic Burger",
        "quantity": 2,
        "price": 12.99,
        "specialInstructions": "No onions"
      }
    ],
    "subtotal": 38.97,
    "tax": 3.12,
    "deliveryCharge": 5.00,
    "total": 47.09,
    "deliveryAddress": "123 Main St, City, State 12345",
    "deliveryInstructions": "Ring doorbell twice",
    "estimatedDeliveryTime": "2026-05-05T10:30:00Z",
    "createdAt": "2026-05-05T10:00:00Z"
  }
}
```

### Get Order by ID

**GET** `/api/v1/orders/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
  "timestamp": "2026-05-05T10:00:00Z",
  "status": 200,
  "message": "Success",
  "data": {
    "id": 1001,
    "userId": 1,
    "status": "PREPARING",
    "items": [
      {
        "id": 1,
        "menuItemId": 1,
        "name": "Classic Burger",
        "quantity": 2,
        "price": 12.99,
        "specialInstructions": "No onions"
      }
    ],
    "subtotal": 38.97,
    "tax": 3.12,
    "deliveryCharge": 5.00,
    "total": 47.09,
    "deliveryAddress": "123 Main St, City, State 12345",
    "deliveryInstructions": "Ring doorbell twice",
    "estimatedDeliveryTime": "2026-05-05T10:30:00Z",
    "createdAt": "2026-05-05T10:00:00Z",
    "updatedAt": "2026-05-05T10:05:00Z"
  }
}
```

---

## Cart Endpoints

### Add Item to Cart

**POST** `/api/v1/cart/items`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Request:**
```json
{
  "menuItemId": 1,
  "quantity": 2,
  "specialInstructions": "No onions"
}
```

**Response:**
```json
{
  "timestamp": "2026-05-05T10:00:00Z",
  "status": 201,
  "message": "Item added to cart",
  "data": {
    "id": 1,
    "menuItemId": 1,
    "name": "Classic Burger",
    "quantity": 2,
    "price": 12.99,
    "specialInstructions": "No onions"
  }
}
```

### Get Cart

**GET** `/api/v1/cart`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
  "timestamp": "2026-05-05T10:00:00Z",
  "status": 200,
  "message": "Success",
  "data": {
    "items": [
      {
        "id": 1,
        "menuItemId": 1,
        "name": "Classic Burger",
        "quantity": 2,
        "price": 12.99,
        "specialInstructions": "No onions"
      }
    ],
    "subtotal": 25.98,
    "tax": 2.08,
    "deliveryCharge": 5.00,
    "total": 33.06
  }
}
```

---

## Payment Endpoints

### Create Payment Intent

**POST** `/api/v1/payments/create-intent`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Request:**
```json
{
  "orderId": 1001,
  "amount": 47.09,
  "currency": "USD"
}
```

**Response:**
```json
{
  "timestamp": "2026-05-05T10:00:00Z",
  "status": 200,
  "message": "Payment intent created",
  "data": {
    "paymentIntentId": "pi_3abc123xyz456",
    "clientSecret": "pi_3abc123xyz456_secret_def789",
    "amount": 47.09,
    "currency": "USD",
    "status": "requires_payment_method"
  }
}
```

### Confirm Payment

**POST** `/api/v1/payments/confirm`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Request:**
```json
{
  "paymentIntentId": "pi_3abc123xyz456",
  "paymentMethodId": "pm_1abc123xyz456"
}
```

**Response:**
```json
{
  "timestamp": "2026-05-05T10:00:00Z",
  "status": 200,
  "message": "Payment confirmed",
  "data": {
    "paymentIntentId": "pi_3abc123xyz456",
    "status": "succeeded",
    "amount": 47.09,
    "currency": "USD",
    "orderId": 1001
  }
}
```

---

## Error Responses

### Validation Error

**Response:**
```json
{
  "timestamp": "2026-05-05T10:00:00Z",
  "status": 400,
  "message": "Validation failed",
  "error": {
    "code": "VALIDATION_ERROR",
    "details": [
      {
        "field": "email",
        "message": "Email is required"
      },
      {
        "field": "password",
        "message": "Password must be at least 8 characters"
      }
    ]
  }
}
```

### Not Found Error

**Response:**
```json
{
  "timestamp": "2026-05-05T10:00:00Z",
  "status": 404,
  "message": "Resource not found",
  "error": {
    "code": "NOT_FOUND",
    "message": "Menu item with ID 999 not found"
  }
}
```

### Unauthorized Error

**Response:**
```json
{
  "timestamp": "2026-05-05T10:00:00Z",
  "status": 401,
  "message": "Unauthorized",
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid or expired token"
  }
}
```

### Internal Server Error

**Response:**
```json
{
  "timestamp": "2026-05-05T10:00:00Z",
  "status": 500,
  "message": "Internal server error",
  "error": {
    "code": "INTERNAL_ERROR",
    "message": "An unexpected error occurred"
  }
}
```

---

## Rate Limiting

All API endpoints are rate-limited to prevent abuse:

- **Public endpoints:** 100 requests per minute
- **Authenticated endpoints:** 200 requests per minute
- **Admin endpoints:** 500 requests per minute

Rate limit headers are included in responses:

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1620192000
```

---

## Pagination

List endpoints support pagination:

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20, max: 100)
- `sort`: Sort field (default: id)
- `direction`: Sort direction (asc/desc, default: asc)

**Example:**
```
GET /api/v1/menu?page=0&size=10&sort=price&direction=desc
```

---

## Filtering and Search

List endpoints support filtering and search:

**Query Parameters:**
- `search`: Search term
- `categoryId`: Filter by category ID
- `available`: Filter by availability (true/false)

**Example:**
```
GET /api/v1/menu?search=burger&categoryId=1&available=true
```

---

## API Versioning

The API uses URL versioning:

- Current version: `/api/v1/`
- Previous versions: `/api/v0/` (deprecated)

---

## Authentication

Most endpoints require authentication using JWT tokens:

**Header:**
```
Authorization: Bearer <token>
```

**Token Expiration:** 1 hour

**Token Refresh:** Use `/api/v1/auth/refresh` endpoint

---

## CORS

The API supports CORS for cross-origin requests:

**Allowed Origins:**
- `http://localhost:3000` (development)
- `https://www.supermalle.com` (production)

**Allowed Methods:** GET, POST, PUT, DELETE, PATCH, OPTIONS

**Allowed Headers:** Authorization, Content-Type, X-Requested-With
