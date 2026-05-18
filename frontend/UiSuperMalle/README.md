# SuperMalle Frontend

React 19 + Vite 8 + Tailwind CSS 4 + Zustand 5 UI for the SuperMalle restaurant management system. Built with a mobile-first, accessible design and real-time WebSocket integration.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Environment Variables](#environment-variables)
- [Routes](#routes)
- [State Management](#state-management)
- [API Integration](#api-integration)
- [Authentication](#authentication)
- [WebSocket Real-time Updates](#websocket-real-time-updates)
- [Component Architecture](#component-architecture)
- [Pages](#pages)
- [Testing](#testing)
- [Build & Deploy](#build--deploy)
- [Troubleshooting](#troubleshooting)

---

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| React | 19.2.4 | UI framework |
| Vite | 8.0.4 | Build tool and dev server |
| Tailwind CSS | 4.2.2 | Utility-first styling |
| Zustand | 5.0.12 | Lightweight state management |
| React Router DOM | 7.14.1 | Client-side routing |
| Axios | 1.15.0 | HTTP client with interceptors |
| Stripe.js / React Stripe | 9.4 / 6.3 | Payment UI components |
| Lucide React | 1.8.0 | Icon library |
| Recharts | 3.8.1 | Charting library |
| Vitest | 4.1.5 | Unit testing framework |
| Testing Library | 16.3.2 | Component testing utilities |
| ESLint | 9.39.4 | Code linting |

---

## Project Structure

```
UiSuperMalle/
├── index.html                        # Entry HTML
├── package.json                      # Dependencies and scripts
├── vite.config.js                    # Vite configuration
├── eslint.config.js                  # ESLint flat config
│
├── public/                           # Static assets
│   ├── favicon.svg                   # Browser tab icon
│   └── icons.svg                     # SVG sprite icons
│
└── src/                              # Application source
    ├── main.jsx                      # React entry point (createRoot)
    ├── App.jsx                       # Root component, router, layouts
    ├── index.css                     # Global Tailwind imports and styles
    │
    ├── api/                          # API integration layer
    │   ├── client.js                 # Axios instance with interceptors
    │   ├── endpoints.js              # All API endpoint URL definitions
    │   └── helpers.js                # Request/response utility functions
    │
    ├── store/                        # Zustand state stores
    │   ├── authStore.js              # Auth state (user, token, login/logout)
    │   ├── cartStore.js              # Cart state (items, add/remove/update)
    │   └── toastStore.js             # Toast notification state
    │
    ├── hooks/                        # Custom React hooks
    │   └── useWebSocket.js           # STOMP WebSocket connection
    │
    ├── lib/                          # Utility libraries
    │   └── validation.js             # Client-side form validation
    │
    ├── components/                   # Reusable UI components
    │   ├── common/                   # Shared components
    │   │   ├── ErrorBoundary.jsx     # React error boundary
    │   │   ├── Loading.jsx           # Loading spinner/skeleton
    │   │   ├── Form.jsx              # Form input components
    │   │   └── ToastContainer.jsx    # Toast notification display
    │   └── layout/                   # Layout components
    │       ├── Navbar.jsx            # Top navigation bar
    │       ├── Footer.jsx            # Site footer
    │       └── AdminSidebar.jsx      # Admin sidebar navigation
    │
    └── pages/                        # Route-level page components
        ├── customer/                 # Customer-facing pages (13)
        │   ├── HomePage.jsx          # Landing/hero page
        │   ├── MenuPage.jsx          # Menu browsing grid
        │   ├── MenuItemPage.jsx      # Single item detail
        │   ├── CartPage.jsx          # Shopping cart view
        │   ├── CheckoutPage.jsx      # Checkout form
        │   ├── OrdersPage.jsx        # Order history list
        │   ├── OrderDetailPage.jsx   # Single order detail
        │   ├── LoginPage.jsx         # Login form
        │   ├── RegisterPage.jsx      # Registration form
        │   ├── ForgotPasswordPage.jsx# Password reset request
        │   ├── ResetPasswordPage.jsx # Password reset form
        │   ├── ProfilePage.jsx       # User profile settings
        │   └── OAuth2CallbackPage.jsx# Google OAuth2 redirect handler
        │
        ├── admin/                    # Admin management pages (11)
        │   ├── AdminDashboard.jsx    # KPI dashboard with charts
        │   ├── AdminOrders.jsx       # Order management table
        │   ├── AdminMenu.jsx         # Menu item CRUD
        │   ├── AdminCategories.jsx   # Category management
        │   ├── AdminCoupons.jsx      # Coupon CRUD
        │   ├── AdminPayments.jsx     # Payment reconciliation
        │   ├── AdminUsers.jsx        # User management
        │   ├── AdminReviews.jsx      # Review moderation
        │   ├── AdminSettings.jsx     # Restaurant settings
        │   ├── AdminOperatingHours.jsx# Hours configuration
        │   └── AdminAnnounce.jsx     # Push announcements
        │
        ├── checkout/                 # Payment pages
        │   └── StripeCheckoutPage.jsx# Stripe Elements payment
        │
        ├── orders/                   # Order tracking pages
        │   ├── OrderConfirmationPage.jsx # Post-order confirmation
        │   └── OrderTrackingPage.jsx # Real-time order tracking
        │
        └── error/                    # Error pages
            ├── NotFoundPage.jsx      # 404 page
            └── ServerErrorPage.jsx   # 500 page
```

---

## Features

### Customer Features

**Menu Browsing** — Browse menu items organized by category with images, prices, dietary tags (vegetarian, vegan, gluten-free), and allergen information. Search by name or description. Filter by category.

**Product Detail** — View full item details with customization options (size, extras, add-ons). Select multiple options and see price adjustments in real-time.

**Shopping Cart** — Add items with customizations. Update quantities, remove items, view running total. Special instructions per item. Cart persists across sessions.

**Checkout** — Choose delivery or pickup. Enter delivery address and special instructions. Pay with Stripe credit card via Stripe Elements.

**Order Tracking** — Real-time order status updates via WebSocket. See status changes: PENDING → CONFIRMED → PREPARING → READY → DELIVERED. Order confirmation page with order summary.

**Order History** — View all past orders with status, items, and total.

**User Authentication** — Register with email/password. Login with JWT. Google OAuth2 social login. Forgot/reset password flow.

**Profile Management** — Update name, email, phone, address. Change password.

**Loyalty Program** — View points balance and tier. Earn points on orders. Redeem for discounts. View leaderboard.

### Admin Features

**Dashboard** — Key performance indicators: total revenue (today/week/month), order count, active orders, customer count. Sales chart (daily/weekly/monthly). Order status distribution. Top selling items. Recent orders list.

**Order Management** — View all orders with filtering by status and date range. Update order status with timestamped audit log. Update estimated preparation time. Cancel orders with reason.

**Menu Management** — Full CRUD for menu items. Add option groups with multiple options. Set dietary tags and allergens. Toggle availability. Upload images. Search and filter.

**Category Management** — Create/edit/delete categories. Drag-to-reorder with sort order. Soft delete (deactivate).

**Coupon Engine** — Create percentage, fixed amount, and BOGO coupons. Set usage limits (total and per-user). Minimum order amount. Valid date range. Track usage statistics.

**Payment Reconciliation** — View all payment transactions. Filter by status (pending, completed, failed, refunded). Filter by date range. Process refunds with reason.

**User Management** — View all users with role filtering. Create new users (generates password, sends welcome email). Update user details. Force password reset.

**Review Moderation** — View pending/approved/rejected reviews. Approve or reject with feedback. View item and user details.

**Operating Hours** — Configure hours for each day of the week. Open/close times. Close restaurant on specific days.

**Settings** — Restaurant name, phone, address. Tax rates (standard, alcohol, prepared food, grocery). Delivery charge.

**Announcements** — Send push notifications to all customers or specific segments.

---

## Prerequisites

- **Node.js 18+** and **npm** (or pnpm/yarn)
- **Backend API** running on port 8080 (see backend README)
- **Stripe account** (test mode for development)
- **Google OAuth2 credentials** (optional, for social login)

---

## Quick Start

```bash
# 1. Install dependencies
npm install

# 2. Configure environment
cp .env.example .env

# 3. Start development server (requires backend on port 8080)
npm run dev
```

The app starts at `http://localhost:5173`. Hot module replacement is enabled.

---

## Environment Variables

Create a `.env` file in `UiSuperMalle/` with the following:

### Required

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_URL` | `http://localhost:8080/api/v1` | Backend REST API base URL |
| `VITE_WS_URL` | `ws://localhost:8080/ws` | WebSocket endpoint for real-time |

### Optional

| Variable | Description |
|----------|-------------|
| `VITE_STRIPE_PUBLIC_KEY` | Stripe publishable key (required for payments) |
| `VITE_GOOGLE_CLIENT_ID` | Google OAuth2 client ID (required for social login) |
| `VITE_ENABLE_ANALYTICS` | Set to `true` to enable Google Analytics |
| `VITE_GA_ID` | Google Analytics measurement ID |
| `VITE_ENABLE_SENTRY` | Set to `true` to enable Sentry error tracking |
| `VITE_SENTRY_DSN` | Sentry Data Source Name |
| `VITE_ENV` | Environment label (`development`, `staging`, `production`) |

### Production

For production builds, use `.env.production`:
```env
VITE_API_URL=https://api.supermalle.com/api/v1
VITE_WS_URL=wss://api.supermalle.com/ws
VITE_STRIPE_PUBLIC_KEY=pk_live_your_stripe_key
VITE_GOOGLE_CLIENT_ID=your_google_client_id
VITE_ENV=production
```

---

## Routes

### Public Routes

| Path | Component | Description |
|------|-----------|-------------|
| `/` | `HomePage` | Landing page with hero, featured items |
| `/menu` | `MenuPage` | Full menu grid with categories |
| `/menu/:id` | `MenuItemPage` | Item detail with customization |
| `/login` | `LoginPage` | Email/password login + Google OAuth2 |
| `/register` | `RegisterPage` | New user registration |
| `/forgot-password` | `ForgotPasswordPage` | Request password reset email |
| `/reset-password` | `ResetPasswordPage` | Set new password with token |
| `/oauth2/callback` | `OAuth2CallbackPage` | Google OAuth2 redirect handler |
| `/500` | `ServerErrorPage` | Internal error display |
| `*` | `NotFoundPage` | 404 page |

### Authenticated Routes (require login)

| Path | Component | Description |
|------|-----------|-------------|
| `/cart` | `CartPage` | Shopping cart with items |
| `/checkout` | `CheckoutPage` | Order form (delivery/pickup) |
| `/checkout/stripe` | `StripeCheckoutPage` | Stripe card payment |
| `/checkout/pay/:orderId` | `StripeCheckoutPage` | Payment for specific order |
| `/orders` | `OrdersPage` | Order history list |
| `/orders/:id` | `OrderDetailPage` | Single order details |
| `/orders/:id/confirmation` | `OrderConfirmationPage` | Post-payment confirmation |
| `/orders/:id/track` | `OrderTrackingPage` | Real-time order tracking |
| `/profile` | `ProfilePage` | Edit profile, change password |

### Admin Routes (require ADMIN role)

| Path | Component | Description |
|------|-----------|-------------|
| `/admin` | `AdminDashboard` | KPI dashboard |
| `/admin/orders` | `AdminOrders` | Order management |
| `/admin/menu` | `AdminMenu` | Menu item CRUD |
| `/admin/categories` | `AdminCategories` | Category management |
| `/admin/coupons` | `AdminCoupons` | Coupon CRUD |
| `/admin/payments` | `AdminPayments` | Payment reconciliation |
| `/admin/users` | `AdminUsers` | User management |
| `/admin/reviews` | `AdminReviews` | Review moderation |
| `/admin/hours` | `AdminOperatingHours` | Operating hours |
| `/admin/settings` | `AdminSettings` | Restaurant settings |
| `/admin/announce` | `AdminAnnounce` | Send announcements |

---

## State Management

The app uses **Zustand** for state management — three lightweight stores:

### authStore

```js
{
  user: { id, name, email, role },
  token: "jwt-token",
  refreshToken: "refresh-token",

  // Actions
  login(email, password),
  register(email, password, name),
  logout(),
  updateProfile(data),
  refreshToken()
}
```

**Key behaviors:**
- Token stored in `localStorage` for persistence across page reloads
- `login()` stores JWT and auto-fetches user profile
- `logout()` clears all auth state and redirects to login
- On 401 API response, auto-attempts token refresh before logging out

### cartStore

```js
{
  items: [{ id, menuItem, quantity, selectedOptions, specialInstructions }],
  loading: false,

  // Computed
  totalItems,
  subtotal,

  // Actions
  fetchCart(),
  addItem(menuItemId, quantity, options),
  updateItemQuantity(cartItemId, quantity),
  removeItem(cartItemId),
  clearCart()
}
```

**Key behaviors:**
- Cart is synced with backend (fetched on login via `fetchCart()`)
- Optimistic updates for add/remove operations
- Loading state prevents double-submissions

### toastStore

```js
{
  toasts: [{ id, message, type: "success" | "error" | "info" }],

  // Actions
  addToast(message, type),
  removeToast(id)
}
```

**Key behaviors:**
- Auto-dismiss after 3 seconds
- Max 5 concurrent toasts
- Types styled differently (green/red/blue)

---

## API Integration

### Axios Client (`api/client.js`)

The Axios instance is configured with:

- **Base URL**: from `VITE_API_URL` env var
- **Request interceptor**: attaches JWT token from `authStore` to every request
- **Response interceptor**: handles 401 (auto-logout), network errors, and generic errors
- **Timeout**: 30 seconds
- **Headers**: `Content-Type: application/json` by default

```js
// Usage example
import client from '../api/client';
import endpoints from '../api/endpoints';

const response = await client.get(endpoints.menu.items);
```

### Endpoints (`api/endpoints.js`)

Centralized endpoint definitions matching the backend REST API:

```js
export const endpoints = {
  auth: {
    login: '/auth/login',
    register: '/auth/register',
    refresh: '/auth/refresh',
    forgotPassword: '/auth/forgot-password',
    resetPassword: '/auth/reset-password',
    changePassword: '/auth/change-password',
  },
  menu: {
    items: '/menu',
    item: (id) => `/menu/${id}`,
    search: '/menu/search',
    category: (id) => `/menu/category/${id}`,
  },
  categories: '/categories',
  cart: {
    base: '/cart',
    add: '/cart/add',
    update: (id) => `/cart/update/${id}`,
    remove: (id) => `/cart/remove/${id}`,
    clear: '/cart/clear',
  },
  orders: {
    base: '/orders',
    detail: (id) => `/orders/${id}`,
    cancel: (id) => `/orders/${id}/cancel`,
  },
  // ... and more
};
```

---

## Authentication

### JWT Flow

1. User submits login form → `POST /auth/login`
2. Backend returns JWT access token + refresh token
3. Tokens stored in `authStore` + `localStorage`
4. Axios interceptor attaches `Authorization: Bearer <token>` to all requests
5. On 401: auto-attempt refresh via `POST /auth/refresh`
6. On refresh failure: auto-logout

### OAuth2 (Google)

1. User clicks "Sign in with Google"
2. Redirected to Google authentication
3. Google redirects back to `/oauth2/callback` with authorization code
4. Callback page exchanges code for JWT via backend
5. Same JWT flow as above

### Route Protection

- **ProtectedRoute**: redirects to `/login` if no valid JWT
- **AdminRoute**: redirects to `/` if user role is not ADMIN
- Applied at the route level in `App.jsx`

---

## WebSocket Real-time Updates

The `useWebSocket` hook connects to the STOMP broker at `VITE_WS_URL`.

### Connected automatically for:

**Customers:**
- Order status updates (`/topic/orders/{orderId}`)
- Loyalty point changes (`/topic/user/{userId}/loyalty`)

**Admins:**
- New orders (`/topic/admin/orders`)
- Order modifications (`/topic/admin/order-modifications`)
- Inventory alerts (`/topic/admin/inventory/alerts`)

### Implementation

```js
import useWebSocket from '../hooks/useWebSocket';

function OrderTracker({ orderId }) {
  const { lastMessage } = useWebSocket(`/topic/orders/${orderId}`);

  useEffect(() => {
    if (lastMessage) {
      const status = JSON.parse(lastMessage.body).status;
      // Update UI with new status
    }
  }, [lastMessage]);
}
```

---

## Component Architecture

### Layout Components

**Navbar** — Logo, navigation links (Home, Menu, Cart, Orders), auth buttons (Login/Logout/Profile). Shows cart item count badge. Responsive — collapses to hamburger menu on mobile.

**Footer** — Restaurant info, links, copyright. Simple and consistent across all customer pages.

**AdminSidebar** — Vertical navigation for admin pages (Dashboard, Orders, Menu, Categories, Coupons, Payments, Users, Reviews, Hours, Settings, Announce). Highlights active route. Collapsible on mobile.

### Common Components

**ErrorBoundary** — Class-based React error boundary. Catches render errors and displays a fallback UI with "Something went wrong" message and retry button.

**Loading** — Full-page or inline spinner. Used during API calls and data fetching.

**Form** — Reusable form components (input, select, textarea, checkbox) with consistent styling and error display.

**ToastContainer** — Fixed-position toast notification display. Renders toasts from `toastStore`. Auto-dismiss animation.

---

## Pages

### Customer Pages (13)

| Page | Key Features |
|------|-------------|
| **HomePage** | Hero banner, featured menu items, category quick-links |
| **MenuPage** | Category tabs, item cards with images/prices, search bar |
| **MenuItemPage** | Image gallery, description, price, option groups, add-to-cart |
| **CartPage** | Item list with quantities, option summary, subtotal, checkout button |
| **CheckoutPage** | Order type (delivery/pickup), address form, coupon code, order summary |
| **OrdersPage** | Order history table with status badges, reorder button |
| **OrderDetailPage** | Full order breakdown, status timeline, tracking link |
| **LoginPage** | Email/password form, Google OAuth2 button, forgot password link |
| **RegisterPage** | Registration form with validation |
| **ForgotPasswordPage** | Email input, sends reset link |
| **ResetPasswordPage** | New password form with token |
| **ProfilePage** | Edit name/email/phone/address, change password form |
| **OAuth2CallbackPage** | Handles Google redirect, exchanges code for token |

### Admin Pages (11)

| Page | Key Features |
|------|-------------|
| **AdminDashboard** | Revenue/order/customer KPIs, sales chart (Recharts), recent orders, top items |
| **AdminOrders** | Filtered table by status/date, status update dropdown, cancel action |
| **AdminMenu** | Item table with search/filter, create/edit modal with option groups, delete |
| **AdminCategories** | Category list with drag reorder, create/edit form, soft delete |
| **AdminCoupons** | Coupon table with status, create/edit form (type, value, limits, dates) |
| **AdminPayments** | Payment table with status/date filters, refund action with reason |
| **AdminUsers** | User table with role filter, create modal (generates password), reset password |
| **AdminReviews** | Review list with approve/reject buttons, status filter |
| **AdminSettings** | Restaurant name, tax rates, delivery charge, contact info form |
| **AdminOperatingHours** | Day-by-day hour editor, clone from previous day, close toggle |
| **AdminAnnounce** | Title/message form, send to all customers |

---

## Testing

```bash
# Run all tests
npm run test

# Run with coverage
npm run test:coverage

# Run with interactive UI
npm run test:ui

# Run in watch mode
npx vitest
```

Testing setup:
- **Vitest** as test runner
- **@testing-library/react** for component rendering
- **@testing-library/jest-dom** for DOM matchers
- **@testing-library/user-event** for user interactions
- **jsdom** as test environment

---

## Build & Deploy

### Production Build

```bash
npm run build
```

Output is in `dist/`. Static files ready for any web server or CDN.

### Deployment Options

| Platform | Setup |
|----------|-------|
| **Vercel** | Connect repo → auto-deploys from `main` branch |
| **Netlify** | Set build command `npm run build`, publish directory `dist` |
| **Docker** | See Dockerfile in repo root |
| **Nginx** | Serve `dist/` → proxy API `/api/*` to backend |

### Docker

```dockerfile
# Multi-stage build
FROM node:22-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

---

## Performance Optimization

- **Code splitting**: Route-level lazy loading via React Router
- **Image optimization**: Responsive images, lazy loading
- **API caching**: ETag support from backend
- **Request debouncing**: Search input debounced (300ms)
- **Optimistic updates**: Cart add/remove updates UI before API responds
- **Bundle analysis**: Run `npx vite-bundle-analyzer` to inspect bundle size

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| **CORS errors** | Ensure backend `FRONTEND_URL` env var matches your frontend origin (http://localhost:5173) |
| **Blank page on build** | Check for runtime errors in browser console. Run `npm run build` and check output |
| **WebSocket connection fails** | Verify `VITE_WS_URL` matches backend WebSocket endpoint. Backend must be running |
| **Stripe payments fail** | Check `VITE_STRIPE_PUBLIC_KEY` is correct and backend has matching secret key |
| **OAuth2 callback fails** | Verify Google OAuth2 redirect URI is configured in Google Cloud Console |
| **API 401 errors** | Token expired. Logout and login again. Check backend JWT expiration config |
| **npm install fails** | Delete `node_modules` and `package-lock.json`, run `npm install` again |
