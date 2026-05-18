import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useEffect } from 'react';
import ErrorBoundary from './components/common/ErrorBoundary';
import ToastContainer from './components/common/ToastContainer';
import useAuthStore from './store/authStore';
import useCartStore from './store/cartStore';
import Navbar from './components/layout/Navbar';
import Footer from './components/layout/Footer';
import AdminSidebar from './components/layout/AdminSidebar';

// Customer pages
import HomePage from './pages/customer/HomePage';
import MenuPage from './pages/customer/MenuPage';
import MenuItemPage from './pages/customer/MenuItemPage';
import CartPage from './pages/customer/CartPage';
import CheckoutPage from './pages/customer/CheckoutPage';
import OrdersPage from './pages/customer/OrdersPage';
import OrderDetailPage from './pages/customer/OrderDetailPage';
import ProfilePage from './pages/customer/ProfilePage';
import LoginPage from './pages/customer/LoginPage';
import RegisterPage from './pages/customer/RegisterPage';
import ForgotPasswordPage from './pages/customer/ForgotPasswordPage';
import ResetPasswordPage from './pages/customer/ResetPasswordPage';
import OAuth2CallbackPage from './pages/customer/OAuth2CallbackPage';
import StripeCheckoutPage from './pages/checkout/StripeCheckoutPage';
import OrderConfirmationPage from './pages/orders/OrderConfirmationPage';
import OrderTrackingPage from './pages/orders/OrderTrackingPage';

// Admin pages
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminOrders from './pages/admin/AdminOrders';
import AdminMenu from './pages/admin/AdminMenu';
import AdminCategories from './pages/admin/AdminCategories';
import AdminCoupons from './pages/admin/AdminCoupons';
import AdminPayments from './pages/admin/AdminPayments';
import AdminSettings from './pages/admin/AdminSettings';
import AdminAnnounce from './pages/admin/AdminAnnounce';
import AdminUsers from './pages/admin/AdminUsers';
import AdminReviews from './pages/admin/AdminReviews';
import AdminOperatingHours from './pages/admin/AdminOperatingHours';

// Error pages
import NotFoundPage from './pages/error/NotFoundPage';
import ServerErrorPage from './pages/error/ServerErrorPage';

function ProtectedRoute({ children }) {
  const { token } = useAuthStore();
  if (!token) return <Navigate to="/login" />;
  return children;
}

function AdminRoute({ children }) {
  const { user, token } = useAuthStore();
  if (!token) return <Navigate to="/login" />;
  if (user?.role !== 'ADMIN') return <Navigate to="/" />;
  return children;
}

function CustomerLayout({ children }) {
  return (
    <div className="min-h-screen flex flex-col bg-bg-base">
      <Navbar />
      <main className="flex-1">{children}</main>
      <Footer />
    </div>
  );
}

function AdminLayout({ children }) {
  return (
    <div className="min-h-screen flex bg-bg-base">
      <AdminSidebar />
      <main className="flex-1 overflow-auto">{children}</main>
    </div>
  );
}

function App() {
  const { token } = useAuthStore();
  const { fetchCart } = useCartStore();

  useEffect(() => {
    if (token) fetchCart();
  }, [token, fetchCart]);

  return (
    <ErrorBoundary>
      <BrowserRouter>
        <Routes>
          {/* Auth */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
          <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />

          {/* Customer */}
          <Route path="/" element={<CustomerLayout><HomePage /></CustomerLayout>} />
          <Route path="/menu" element={<CustomerLayout><MenuPage /></CustomerLayout>} />
          <Route path="/menu/:id" element={<CustomerLayout><MenuItemPage /></CustomerLayout>} />
          <Route path="/cart" element={<CustomerLayout><ProtectedRoute><CartPage /></ProtectedRoute></CustomerLayout>} />
          <Route path="/checkout" element={<CustomerLayout><ProtectedRoute><CheckoutPage /></ProtectedRoute></CustomerLayout>} />
          <Route path="/checkout/stripe" element={<CustomerLayout><ProtectedRoute><StripeCheckoutPage /></ProtectedRoute></CustomerLayout>} />
          <Route path="/checkout/pay/:orderId" element={<CustomerLayout><ProtectedRoute><StripeCheckoutPage /></ProtectedRoute></CustomerLayout>} />
          <Route path="/orders" element={<CustomerLayout><ProtectedRoute><OrdersPage /></ProtectedRoute></CustomerLayout>} />
          <Route path="/orders/:id" element={<CustomerLayout><ProtectedRoute><OrderDetailPage /></ProtectedRoute></CustomerLayout>} />
          <Route path="/orders/:id/confirmation" element={<CustomerLayout><ProtectedRoute><OrderConfirmationPage /></ProtectedRoute></CustomerLayout>} />
          <Route path="/orders/:id/track" element={<CustomerLayout><ProtectedRoute><OrderTrackingPage /></ProtectedRoute></CustomerLayout>} />
          <Route path="/profile" element={<CustomerLayout><ProtectedRoute><ProfilePage /></ProtectedRoute></CustomerLayout>} />

          {/* Admin */}
          <Route path="/admin" element={<AdminRoute><AdminLayout><AdminDashboard /></AdminLayout></AdminRoute>} />
          <Route path="/admin/orders" element={<AdminRoute><AdminLayout><AdminOrders /></AdminLayout></AdminRoute>} />
          <Route path="/admin/menu" element={<AdminRoute><AdminLayout><AdminMenu /></AdminLayout></AdminRoute>} />
          <Route path="/admin/categories" element={<AdminRoute><AdminLayout><AdminCategories /></AdminLayout></AdminRoute>} />
          <Route path="/admin/coupons" element={<AdminRoute><AdminLayout><AdminCoupons /></AdminLayout></AdminRoute>} />
          <Route path="/admin/payments" element={<AdminRoute><AdminLayout><AdminPayments /></AdminLayout></AdminRoute>} />
          <Route path="/admin/settings" element={<AdminRoute><AdminLayout><AdminSettings /></AdminLayout></AdminRoute>} />
          <Route path="/admin/users" element={<AdminRoute><AdminLayout><AdminUsers /></AdminLayout></AdminRoute>} />
          <Route path="/admin/reviews" element={<AdminRoute><AdminLayout><AdminReviews /></AdminLayout></AdminRoute>} />
          <Route path="/admin/hours" element={<AdminRoute><AdminLayout><AdminOperatingHours /></AdminLayout></AdminRoute>} />
          <Route path="/admin/announce" element={<AdminRoute><AdminLayout><AdminAnnounce /></AdminLayout></AdminRoute>} />

          {/* Error Pages */}
          <Route path="/500" element={<ServerErrorPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>

        {/* Toast Notifications */}
        <ToastContainer />
      </BrowserRouter>
    </ErrorBoundary>
  );
}

export default App;
