# Quick Reference Guide - Phase 2 Features

**Last Updated:** 2025-01-XX
**Version:** 2.0.0

## Table of Contents

1. [Stripe Payment Integration](#stripe-payment-integration)
2. [Form Validation](#form-validation)
3. [Loading States](#loading-states)
4. [Order Management](#order-management)
5. [Common Patterns](#common-patterns)

---

## Stripe Payment Integration

### Basic Usage

```jsx
import StripeCheckoutPage from '@/pages/checkout/StripeCheckoutPage';

// In your component
function CartPage() {
  const { items, total } = useCartStore();

  const handleCheckout = () => {
    if (items.length === 0) {
      alert('Your cart is empty');
      return;
    }
    navigate('/checkout/stripe');
  };

  return (
    <button onClick={handleCheckout}>
      Proceed to Checkout
    </button>
  );
}
```

### Payment Flow

1. User navigates to `/checkout/stripe`
2. Reviews order summary
3. Enters card details via Stripe Elements
4. Clicks "Pay" button
5. Payment is processed securely
6. On success, redirects to `/orders/:id/confirmation`
7. Cart is automatically cleared

### Environment Variables

```env
# .env
VITE_STRIPE_PUBLISHABLE_KEY=pk_test_...
VITE_API_URL=http://localhost:8080/api/v1
```

### Backend Integration

The checkout page expects these backend endpoints:

```javascript
// Create payment intent
POST /api/v1/payments/create-payment-intent
{
  amount: 1000, // in cents
  currency: 'usd',
  items: [...]
}

// Get order details
GET /api/v1/orders/:id
```

---

## Form Validation

### Basic Form Example

```jsx
import { Form, FormInput, FormSubmitButton } from '@/components/common/Form';
import { loginSchema } from '@/lib/validation';

function LoginPage() {
  const handleSubmit = async (data, { reset }) => {
    try {
      await client.post('/auth/login', data);
      reset();
      navigate('/dashboard');
    } catch (error) {
      console.error('Login failed:', error);
    }
  };

  return (
    <Form schema={loginSchema} onSubmit={handleSubmit}>
      <FormInput
        name="email"
        label="Email"
        type="email"
        placeholder="you@example.com"
        required
      />
      <FormInput
        name="password"
        label="Password"
        type="password"
        placeholder="••••••••"
        required
      />
      <FormSubmitButton text="Login" />
    </Form>
  );
}
```

### Available Form Components

#### FormInput
```jsx
<FormInput
  name="fieldName"
  label="Field Label"
  type="text"
  placeholder="Placeholder text"
  required
  disabled={false}
/>
```

#### FormTextarea
```jsx
<FormTextarea
  name="description"
  label="Description"
  placeholder="Enter description..."
  rows={4}
  required
/>
```

#### FormSelect
```jsx
<FormSelect
  name="category"
  label="Category"
  placeholder="Select a category"
  required
  options={[
    { value: '1', label: 'Category 1' },
    { value: '2', label: 'Category 2' }
  ]}
/>
```

#### FormCheckbox
```jsx
<FormCheckbox
  name="agree"
  label="I agree to the terms and conditions"
  required
/>
```

#### FormRadioGroup
```jsx
<FormRadioGroup
  name="deliveryType"
  label="Delivery Type"
  required
  options={[
    { value: 'delivery', label: 'Delivery' },
    { value: 'pickup', label: 'Pickup' }
  ]}
/>
```

### Available Validation Schemas

```javascript
import { schemas } from '@/lib/validation';

// Auth
schemas.login
schemas.register
schemas.forgotPassword
schemas.resetPassword

// Profile
schemas.updateProfile
schemas.changePassword

// Order
schemas.deliveryAddress
schemas.pickupOrder

// Review
schemas.review

// Contact
schemas.contact
schemas.newsletter

// Loyalty
schemas.redeemPoints

// Admin
schemas.menuItem
schemas.category
schemas.inventory
```

### Custom Validation Schema

```javascript
import { z } from 'zod';

export const customSchema = z.object({
  name: z.string().min(1, 'Name is required'),
  email: z.string().email('Invalid email'),
  age: z.number().min(18, 'Must be 18 or older')
});
```

---

## Loading States

### Basic Usage

```jsx
import {
  LoadingSpinner,
  FullPageLoading,
  ButtonLoading
} from '@/components/common/Loading';

function MyComponent() {
  const [loading, setLoading] = useState(false);

  const handleClick = async () => {
    setLoading(true);
    try {
      await someAsyncOperation();
    } finally {
      setLoading(false);
    }
  };

  return (
    <button onClick={handleClick} disabled={loading}>
      {loading ? <ButtonLoading /> : 'Submit'}
    </button>
  );
}
```

### Available Loading Components

#### LoadingSpinner
```jsx
<LoadingSpinner size="sm" color="indigo" />
// sizes: sm, md, lg, xl
// colors: indigo, white, gray
```

#### FullPageLoading
```jsx
<FullPageLoading message="Loading your data..." />
```

#### Skeleton Screens
```jsx
<CardSkeleton count={3} />
<MenuItemSkeleton count={6} />
<OrderItemSkeleton count={5} />
<TableSkeleton rows={5} columns={4} />
<ProfileSkeleton />
```

#### Button Loading
```jsx
<button disabled={loading}>
  {loading ? <ButtonLoading text="Processing..." /> : 'Submit'}
</button>
```

#### Inline Loading
```jsx
<InlineLoading text="Loading..." />
```

#### Dots Loading
```jsx
<DotsLoading color="indigo" />
```

#### Progress Bar
```jsx
<ProgressBarLoading progress={75} />
```

#### Loading Overlay
```jsx
{loading && <LoadingOverlay message="Processing..." />}
```

#### Empty State Loading
```jsx
<EmptyStateLoading message="Loading data..." />
```

---

## Order Management

### Order Confirmation

```jsx
import OrderConfirmationPage from '@/pages/orders/OrderConfirmationPage';

// After successful payment
navigate(`/orders/${orderId}/confirmation`);
```

### Order Tracking

```jsx
import OrderTrackingPage from '@/pages/orders/OrderTrackingPage';

// Track order status
navigate(`/orders/${orderId}/track`);
```

### Order Status Flow

```
Pending → Confirmed → Preparing → Ready → Delivered
```

### WebSocket Integration

```jsx
import { useWebSocket } from '@/hooks/useWebSocket';

function OrderTrackingPage() {
  const { lastMessage, connectionStatus } = useWebSocket();

  useEffect(() => {
    if (lastMessage?.type === 'ORDER_UPDATE') {
      // Handle order update
      setOrder(lastMessage.data);
    }
  }, [lastMessage]);

  return (
    <div>
      <div>Connection: {connectionStatus}</div>
      {/* Display order status */}
    </div>
  );
}
```

---

## Common Patterns

### API Call with Loading

```jsx
import { useState } from 'react';
import client from '@/lib/client';
import { InlineLoading } from '@/components/common/Loading';

function MyComponent() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await client.get('/api/endpoint');
      setData(response.data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <InlineLoading />;
  if (error) return <div>Error: {error}</div>;

  return <div>{/* Render data */}</div>;
}
```

### Form with API Submission

```jsx
import { Form, FormInput, FormSubmitButton } from '@/components/common/Form';
import { contactSchema } from '@/lib/validation';
import client from '@/lib/client';
import { useToastStore } from '@/store/toastStore';

function ContactForm() {
  const { addToast } = useToastStore();

  const handleSubmit = async (data, { reset }) => {
    try {
      await client.post('/contact', data);
      addToast({
        type: 'success',
        title: 'Success',
        message: 'Message sent successfully!'
      });
      reset();
    } catch (error) {
      addToast({
        type: 'error',
        title: 'Error',
        message: 'Failed to send message'
      });
    }
  };

  return (
    <Form schema={contactSchema} onSubmit={handleSubmit}>
      <FormInput name="name" label="Name" required />
      <FormInput name="email" label="Email" type="email" required />
      <FormTextarea name="message" label="Message" required />
      <FormSubmitButton text="Send Message" />
    </Form>
  );
}
```

### Protected Route

```jsx
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

function ProtectedComponent() {
  const { token } = useAuthStore();
  const navigate = useNavigate();

  useEffect(() => {
    if (!token) {
      navigate('/login');
    }
  }, [token, navigate]);

  if (!token) return null;

  return <div>Protected content</div>;
}
```

### Error Handling

```jsx
import { useToastStore } from '@/store/toastStore';

function MyComponent() {
  const { addToast } = useToastStore();

  const handleError = (error) => {
    const message = error.response?.data?.message || 'An error occurred';
    addToast({
      type: 'error',
      title: 'Error',
      message
    });
  };

  const handleSuccess = (message) => {
    addToast({
      type: 'success',
      title: 'Success',
      message
    });
  };

  return <div>{/* Component content */}</div>;
}
```

### Loading State with Skeleton

```jsx
import { useState, useEffect } from 'react';
import { MenuItemSkeleton } from '@/components/common/Loading';

function MenuPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchItems();
  }, []);

  const fetchItems = async () => {
    setLoading(true);
    try {
      const response = await client.get('/menu');
      setItems(response.data);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <MenuItemSkeleton count={6} />;
  }

  return (
    <div>
      {items.map(item => (
        <div key={item.id}>{item.name}</div>
      ))}
    </div>
  );
}
```

---

## Tips & Best Practices

### Form Validation
- Always use validation schemas for forms
- Provide clear error messages
- Validate on blur for better UX
- Use loading states during submission

### Loading States
- Use skeleton screens for content loading
- Show loading indicators for async operations
- Provide feedback for long-running operations
- Use appropriate loading type for context

### Error Handling
- Always handle API errors gracefully
- Show user-friendly error messages
- Log errors for debugging
- Provide recovery options

### Performance
- Use code splitting for large components
- Implement lazy loading for images
- Debounce form inputs
- Cancel pending requests

### Accessibility
- Add ARIA labels to interactive elements
- Ensure keyboard navigation works
- Provide alt text for images
- Use semantic HTML

---

## Troubleshooting

### Stripe Payment Issues

**Problem:** Payment fails with error
**Solution:**
- Check Stripe keys in `.env`
- Verify backend payment endpoint
- Check network connection
- Review Stripe dashboard for errors

### Form Validation Issues

**Problem:** Validation not working
**Solution:**
- Verify schema is imported correctly
- Check field names match schema
- Ensure form is wrapped in `<Form>` component
- Check console for errors

### Loading State Issues

**Problem:** Loading not showing
**Solution:**
- Verify loading state is set correctly
- Check component is rendering
- Ensure loading component is imported
- Review state management

### WebSocket Issues

**Problem:** WebSocket not connecting
**Solution:**
- Check WebSocket URL in `.env`
- Verify backend WebSocket support
- Check network connection
- Review browser console for errors

---

## Additional Resources

### Documentation
- [Phase 2 Implementation Summary](./PHASE2_IMPLEMENTATION_SUMMARY.md)
- [Production Checklist](./PRODUCTION_CHECKLIST.md)
- [Session Summary](./SESSION_SUMMARY.md)

### External Resources
- [Stripe Documentation](https://stripe.com/docs)
- [React Hook Form](https://react-hook-form.com/)
- [Zod Documentation](https://zod.dev/)
- [React Router](https://reactrouter.com/)

---

## Support

For questions or issues:
- Review the documentation
- Check the GitHub issues
- Contact the development team

**Last Updated:** 2025-01-XX
**Version:** 2.0.0
