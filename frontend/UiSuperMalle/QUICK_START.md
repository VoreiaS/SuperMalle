# Quick Start Guide

Get the SuperMalle UI up and running in 5 minutes.

## Prerequisites

- Node.js 18+ installed
- Backend API running on `http://localhost:8080`
- Git (for cloning)

## Step 1: Clone and Install

```bash
# Navigate to the project directory
cd /home/kai/Downloads/uiForTheSuperMalle/UiSuperMalle

# Install dependencies (already done, but if needed)
npm install
```

## Step 2: Configure Environment

```bash
# Copy the example environment file
cp .env.example .env

# Edit .env with your configuration
nano .env
```

**Minimum required configuration:**
```env
VITE_API_URL=http://localhost:8080/api/v1
VITE_WS_URL=ws://localhost:8080/ws
```

## Step 3: Start Development Server

```bash
npm run dev
```

The application will be available at: **http://localhost:5173**

## Step 4: Test the Application

### Test Customer Flow:
1. Open http://localhost:5173
2. Click "Sign up" to create an account
3. Browse the menu at http://localhost:5173/menu
4. Add items to cart
5. Proceed to checkout

### Test Admin Flow:
1. Register with email: `admin@supermalle.com`
2. The backend should assign ADMIN role
3. Login and access admin dashboard at http://localhost:5173/admin

## Common Issues

### Issue: "Cannot connect to backend"
**Solution:** Ensure your Spring Boot backend is running on port 8080

### Issue: "CORS errors"
**Solution:** Check backend CORS configuration allows `http://localhost:5173`

### Issue: "WebSocket connection failed"
**Solution:** Verify `VITE_WS_URL` is correct and backend WebSocket is enabled

### Issue: "Stripe payments not working"
**Solution:** Add your Stripe test key to `.env`:
```env
VITE_STRIPE_PUBLIC_KEY=pk_test_your_key_here
```

## Development Tips

### Hot Reload
The development server supports hot module replacement. Changes to React components will automatically reload.

### Debugging
Open browser DevTools (F12) to:
- View console logs
- Inspect network requests
- Debug React components with React DevTools

### Testing
Run tests with:
```bash
npm run test
```

### Building for Production
```bash
npm run build
npm run preview
```

## Next Steps

1. Read the full [README.md](README.md) for detailed documentation
2. Check [IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md) for what's been implemented
3. Review [PRODUCTION_GAP_ANALYSIS.md](PRODUCTION_GAP_ANALYSIS.md) for remaining work

## Need Help?

- Check the documentation in the project
- Review error messages in browser console
- Ensure backend API is running and accessible
- Verify environment variables are correctly set

## Default Credentials

If you're using the backend's DataInitializer:

**Admin User:**
- Email: `admin@supermalle.com`
- Password: `admin123`

**Test Customer:**
- Register a new account through the UI

## Features to Try

- ✅ Browse menu with search and filters
- ✅ Add items to cart
- ✅ Update quantities
- ✅ Checkout with delivery/pickup
- ✅ View order history
- ✅ Update profile
- ✅ Admin dashboard (if admin)
- ✅ Manage orders (if admin)
- ✅ Toast notifications
- ✅ Error handling
- ✅ Responsive design

## What's New

This version includes:
- 🎉 Complete error handling system
- 🎉 OAuth2 callback implementation
- 🎉 WebSocket real-time updates
- 🎉 Enhanced API client with retry logic
- 🎉 Improved state management
- 🎉 Toast notification system
- 🎉 Error pages (404, 500)
- 🎉 Environment configuration

Enjoy building with SuperMalle UI! 🚀
