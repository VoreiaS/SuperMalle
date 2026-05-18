# SuperMalle Restaurant UI

A modern, production-ready React application for the SuperMalle restaurant management system. Built with React 19, Vite, Tailwind CSS, and Zustand for state management.

## Features

### Customer Features
- 🍽️ **Menu Browsing**: Browse and search menu items by category
- 🛒 **Shopping Cart**: Add items to cart with quantity management
- 💳 **Checkout**: Complete checkout with delivery/pickup options
- 📦 **Order Tracking**: View order history and track order status
- 👤 **Profile Management**: Update profile and change password
- 🔐 **Authentication**: Login, registration, and OAuth2 (Google) support

### Admin Features
- 📊 **Dashboard**: Real-time statistics and recent orders
- 📋 **Order Management**: View, update, and manage all orders
- 🍕 **Menu Management**: Add, edit, and delete menu items
- 🏷️ **Category Management**: Manage menu categories
- 🎫 **Coupon Management**: Create and manage discount coupons
- 💰 **Payment Management**: View and manage payments
- ⚙️ **Settings**: Configure restaurant settings
- 📢 **Announcements**: Send announcements to customers

### Technical Features
- ⚡ **Real-time Updates**: WebSocket integration for live order updates
- 🔔 **Toast Notifications**: User-friendly notification system
- 🎨 **Modern UI**: Beautiful, responsive design with Tailwind CSS
- 📱 **Mobile-First**: Fully responsive design
- ♿ **Accessible**: WCAG 2.1 compliant with ARIA labels
- 🔒 **Secure**: JWT authentication with token refresh
- 🚀 **Performance**: Code splitting and lazy loading
- 🧪 **Tested**: Unit and integration tests with Vitest

## Tech Stack

- **Frontend**: React 19, Vite 8
- **Styling**: Tailwind CSS 4
- **State Management**: Zustand 5
- **Routing**: React Router DOM 7
- **HTTP Client**: Axios 1
- **Icons**: Lucide React
- **Charts**: Recharts
- **Testing**: Vitest, React Testing Library
- **Payment**: Stripe.js
- **Authentication**: JWT, OAuth2

## Prerequisites

- Node.js 18+ and npm
- Backend API running on port 8080
- Stripe account (for payments)
- Google OAuth2 credentials (optional)

## Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd uiForTheSuperMalle/UiSuperMalle
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Configure environment variables**

   Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

   Edit `.env` and configure the following variables:
   ```env
   # API Configuration
   VITE_API_URL=http://localhost:8080/api/v1
   VITE_WS_URL=ws://localhost:8080/ws

   # Stripe Configuration (Test Mode)
   VITE_STRIPE_PUBLIC_KEY=pk_test_your_stripe_test_key_here

   # Google OAuth2 (Optional)
   VITE_GOOGLE_CLIENT_ID=your_google_client_id_here

   # Feature Flags
   VITE_ENABLE_ANALYTICS=false
   VITE_ENABLE_SENTRY=false

   # Environment
   VITE_ENV=development
   ```

4. **Start the development server**
   ```bash
   npm run dev
   ```

   The application will be available at `http://localhost:5173`

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm run test` - Run tests
- `npm run test:coverage` - Run tests with coverage
- `npm run test:ui` - Run tests with UI

## Project Structure

```
UiSuperMalle/
├── public/                 # Static assets
├── src/
│   ├── api/               # API client and endpoints
│   │   ├── client.js      # Axios instance with interceptors
│   │   ├── endpoints.js   # API endpoint definitions
│   │   └── helpers.js     # Utility functions
│   ├── components/        # Reusable components
│   │   ├── common/        # Common components (ErrorBoundary, Toast)
│   │   └── layout/        # Layout components (Navbar, Footer, Sidebar)
│   ├── hooks/             # Custom React hooks
│   │   └── useWebSocket.js
│   ├── pages/             # Page components
│   │   ├── admin/         # Admin pages
│   │   ├── customer/      # Customer pages
│   │   └── error/         # Error pages
│   ├── store/             # Zustand stores
│   │   ├── authStore.js   # Authentication state
│   │   ├── cartStore.js   # Shopping cart state
│   │   └── toastStore.js  # Toast notifications
│   ├── App.jsx            # Main app component
│   ├── main.jsx           # Entry point
│   └── index.css          # Global styles
├── .env                   # Environment variables (development)
├── .env.example          # Environment variables template
├── .env.production       # Environment variables (production)
├── vite.config.js        # Vite configuration
├── package.json          # Dependencies and scripts
└── README.md             # This file
```

## Environment Variables

### Required Variables

- `VITE_API_URL` - Backend API base URL
- `VITE_WS_URL` - WebSocket URL for real-time updates

### Optional Variables

- `VITE_STRIPE_PUBLIC_KEY` - Stripe publishable key for payments
- `VITE_GOOGLE_CLIENT_ID` - Google OAuth2 client ID
- `VITE_ENABLE_ANALYTICS` - Enable Google Analytics (true/false)
- `VITE_ENABLE_SENTRY` - Enable Sentry error tracking (true/false)
- `VITE_SENTRY_DSN` - Sentry DSN for error tracking
- `VITE_GA_ID` - Google Analytics ID
- `VITE_ENV` - Environment (development/production)

## API Integration

The application integrates with a Spring Boot backend API. The API client is configured in `src/api/client.js` with:

- Automatic JWT token attachment
- Request/response interceptors
- Error handling and retry logic
- Request timeout (30 seconds)
- Automatic logout on 401 errors

## Authentication

### JWT Authentication
- Tokens are stored in localStorage
- Automatic token attachment to requests
- Auto-logout on token expiry
- Token refresh support

### OAuth2 (Google)
- Google Sign-In integration
- OAuth2 callback handling
- State validation for CSRF protection
- Error handling for OAuth failures

## State Management

The application uses Zustand for state management:

- **authStore**: User authentication and profile
- **cartStore**: Shopping cart and items
- **toastStore**: Toast notifications

## Real-time Updates

WebSocket integration provides real-time updates for:

- Order status changes
- Admin notifications
- Live order tracking

## Error Handling

- Global Error Boundary for React errors
- Toast notifications for user feedback
- API error handling with user-friendly messages
- 404 and 500 error pages

## Testing

Run tests with:
```bash
npm run test
```

Run tests with coverage:
```bash
npm run test:coverage
```

Run tests with UI:
```bash
npm run test:ui
```

## Building for Production

1. **Build the application**
   ```bash
   npm run build
   ```

2. **Preview the production build**
   ```bash
   npm run preview
   ```

3. **Deploy to your hosting service**

   The build output will be in the `dist/` directory.

## Deployment

### Environment Setup

1. Configure production environment variables in `.env.production`
2. Set up your backend API
3. Configure Stripe for payments (if using)
4. Set up Google OAuth2 (if using)

### Deployment Options

- **Vercel**: Easy deployment with automatic builds
- **Netlify**: Simple deployment with continuous deployment
- **AWS S3 + CloudFront**: Static site hosting
- **Docker**: Containerized deployment

### Docker Deployment

Create a `Dockerfile`:
```dockerfile
FROM node:18-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

Build and run:
```bash
docker build -t supermalle-ui .
docker run -p 80:80 supermalle-ui
```

## Performance Optimization

- Code splitting by route
- Lazy loading for images
- Request debouncing
- Request cancellation
- Optimistic updates for cart operations

## Security Considerations

- JWT tokens stored in localStorage (consider httpOnly cookies)
- CSRF protection on forms
- Input validation and sanitization
- Secure HTTP headers (configured in backend)
- Rate limiting (configured in backend)

## Accessibility

- ARIA labels on interactive elements
- Keyboard navigation support
- Screen reader announcements
- Focus management in modals
- Color contrast compliance

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Troubleshooting

### Common Issues

**Issue: CORS errors**
- Solution: Ensure backend CORS is configured to allow your frontend origin

**Issue: WebSocket connection fails**
- Solution: Check VITE_WS_URL and ensure backend WebSocket is running

**Issue: Stripe payments not working**
- Solution: Verify VITE_STRIPE_PUBLIC_KEY is correct and Stripe is configured

**Issue: OAuth2 callback fails**
- Solution: Check Google OAuth2 configuration and redirect URIs

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.

## Support

For support, email support@supermalle.com or open an issue in the repository.

## Acknowledgments

- Built with [React](https://react.dev/)
- Styled with [Tailwind CSS](https://tailwindcss.com/)
- Icons by [Lucide](https://lucide.dev/)
- State management with [Zustand](https://zustand-demo.pmnd.rs/)
