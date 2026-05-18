# SuperMalle Frontend

React 19 + Vite 8 + Tailwind CSS 4 + Zustand 5 UI for the SuperMalle restaurant management system.

```
UiSuperMalle/
├── package.json / vite.config.js / eslint.config.js
├── index.html
└── src/
    ├── main.jsx / App.jsx / index.css
    ├── api/              # Axios client + endpoint map
    ├── store/            # Zustand (auth, cart, toast)
    ├── hooks/            # useWebSocket
    ├── lib/              # Validation utilities
    ├── components/
    │   ├── common/       # ErrorBoundary, Loading, Form, ToastContainer
    │   └── layout/       # Navbar, Footer, AdminSidebar
    └── pages/
        ├── customer/     # 13 pages (home, menu, cart, checkout, orders, auth…)
        ├── admin/        # 11 pages (dashboard, orders, menu, categories, coupons…)
        ├── checkout/     # StripeCheckoutPage
        ├── orders/       # OrderConfirmationPage, OrderTrackingPage
        └── error/        # NotFoundPage, ServerErrorPage
```

## Quick Start

```bash
npm install
cp .env.example .env    # Configure API URL and Stripe key
npm run dev             # Starts at http://localhost:5173
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_URL` | http://localhost:8080/api/v1 | Backend base URL |
| `VITE_WS_URL` | ws://localhost:8080/ws | WebSocket URL |
| `VITE_STRIPE_PUBLIC_KEY` | pk_test_... | Stripe publishable key |
| `VITE_GOOGLE_CLIENT_ID` | - | Google OAuth2 client ID |

## Available Scripts

| Command | Action |
|---------|--------|
| `npm run dev` | Start dev server |
| `npm run build` | Production build |
| `npm run preview` | Preview production build |
| `npm run lint` | ESLint |
| `npm run test` | Vitest |

## Routes

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
| `/cart` | CartPage | Auth |
| `/checkout` / `checkout/stripe` / `checkout/pay/:orderId` | Checkout | Auth |
| `/orders` / `/orders/:id` | Orders / Detail | Auth |
| `/orders/:id/confirmation` | OrderConfirmationPage | Auth |
| `/orders/:id/track` | OrderTrackingPage | Auth |
| `/profile` | ProfilePage | Auth |
| `/admin` | AdminDashboard | Admin |
| `/admin/orders` / `menu` / `categories` / `coupons` / `payments` | Admin CRUD | Admin |
| `/admin/users` / `reviews` / `hours` / `settings` / `announce` | Admin | Admin |
| `/500` / `*` | Error pages | Public |

## Tech Stack

React 19, Vite 8, Tailwind CSS 4, Zustand 5, React Router 7, Axios, Stripe.js, Lucide Icons, Recharts, Vitest.
