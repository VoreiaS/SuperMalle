import client from './client';

// --- Auth ---
export const authApi = {
  register: (data) => client.post('/auth/register', data),
  login: (data) => client.post('/auth/login', data),
  forgotPassword: (email) => client.post('/auth/forgot-password', { email }),
  resetPassword: (token, newPassword) => client.post('/auth/reset-password', { token, newPassword }),
};

// --- Users ---
export const userApi = {
  getMe: () => client.get('/auth/me'),
  updateProfile: (data) => client.put('/auth/me', data),
  updatePassword: (data) => client.put('/auth/me/password', data),
};

// --- Menu (public) ---
export const menuApi = {
  list: (params = {}) => client.get('/menu', { params }),
  get: (id) => client.get(`/menu/${id}`),
};

// --- Categories (public) ---
export const categoryApi = {
  list: () => client.get('/categories'),
};

// --- Cart ---
export const cartApi = {
  get: () => client.get('/cart'),
  addItem: (data) => client.post('/cart/items', data),
  updateItem: (itemId, data) => client.put(`/cart/items/${itemId}`, data),
  removeItem: (itemId) => client.delete(`/cart/items/${itemId}`),
  clear: () => client.delete('/cart'),
};

// --- Orders ---
export const orderApi = {
  place: (data) => client.post('/orders', data),
  list: (params = {}) => client.get('/orders', { params }),
  get: (id) => client.get(`/orders/${id}`),
  getStatus: (id) => client.get(`/orders/${id}/status`),
  cancel: (id) => client.post(`/orders/${id}/cancel`),
  review: (id, data) => client.post(`/orders/${id}/review`, data),
  // Backend-aligned methods
  getMyOrders: (params = {}) => client.get('/orders', { params }),
  getById: (id) => client.get(`/orders/${id}`),
  cancelOrder: (id, data) => client.post(`/orders/${id}/cancel`, data),
};

// --- Payments ---
export const paymentApi = {
  createIntent: (data) => client.post('/payments/create-intent', data),
  getByOrder: (orderId) => client.get(`/payments/order/${orderId}`),
};

// ==================== ADMIN ====================

// --- Admin Menu Items ---
export const adminMenuApi = {
  list: (params = {}) => client.get('/admin/menu', { params }),
  get: (id) => client.get(`/admin/menu/${id}`),
  create: (data) => client.post('/admin/menu', data),
  update: (id, data) => client.put(`/admin/menu/${id}`, data),
  delete: (id) => client.delete(`/admin/menu/${id}`),
};

// --- Admin Categories ---
export const adminCategoryApi = {
  list: () => client.get('/admin/categories'),
  get: (id) => client.get(`/admin/categories/${id}`),
  create: (data) => client.post('/admin/categories', data),
  update: (id, data) => client.put(`/admin/categories/${id}`, data),
  delete: (id) => client.delete(`/admin/categories/${id}`),
};

// --- Admin Users ---
export const adminUserApi = {
  list: (params = {}) => client.get('/admin/users', { params }),
  get: (id) => client.get(`/admin/users/${id}`),
  create: (data) => client.post('/admin/users', data),
  update: (id, data) => client.put(`/admin/users/${id}`, data),
  toggleActive: (id) => client.patch(`/admin/users/${id}/toggle-active`),
  delete: (id) => client.delete(`/admin/users/${id}`),
  resetPassword: (id, data) => client.post(`/admin/users/${id}/reset-password`, data),
};

// --- Admin Orders ---
export const adminOrderApi = {
  list: (params = {}) => client.get('/admin/orders', { params }),
  get: (id) => client.get(`/admin/orders/${id}`),
  updateStatus: (id, status) => client.put(`/admin/orders/${id}/status`, { status }),
  cancel: (id, reason) => client.post(`/admin/orders/${id}/cancel`, { reason }),
};

// --- Admin Dashboard ---
export const adminDashboardApi = {
  stats: () => client.get('/admin/dashboard/stats'),
  recentOrders: () => client.get('/admin/dashboard/recent-orders'),
  charts: (params = {}) => client.get('/admin/dashboard/charts', { params }),
  topItems: (params = {}) => client.get('/admin/dashboard/top-items', { params }),
};

// --- Admin Coupons ---
export const adminCouponApi = {
  list: () => client.get('/admin/coupons'),
  get: (id) => client.get(`/admin/coupons/${id}`),
  create: (data) => client.post('/admin/coupons', data),
  update: (id, data) => client.put(`/admin/coupons/${id}`, data),
  delete: (id) => client.delete(`/admin/coupons/${id}`),
};

// --- Admin Payments ---
export const adminPaymentApi = {
  list: (params = {}) => client.get('/admin/payments', { params }),
  get: (id) => client.get(`/admin/payments/${id}`),
  refund: (paymentId, data) => client.post(`/admin/payments/${paymentId}/refund`, data),
};

// --- Admin Settings ---
export const adminSettingsApi = {
  list: () => client.get('/admin/settings'),
  get: (key) => client.get(`/admin/settings/${key}`),
  update: (data) => client.put('/admin/settings', data),
  delete: (key) => client.delete(`/admin/settings/${key}`),
};

// --- Admin Notifications ---
export const adminNotificationApi = {
  announce: (message) => client.post('/admin/notifications/announce', { message }),
};

// --- Admin Reviews ---
export const adminReviewApi = {
  list: (params = {}) => client.get('/admin/reviews', { params }),
  get: (id) => client.get(`/admin/reviews/${id}`),
  approve: (id) => client.post(`/admin/reviews/${id}/approve`),
  reject: (id, notes) => client.post(`/admin/reviews/${id}/reject`, { notes }),
  delete: (id) => client.delete(`/admin/reviews/${id}`),
};

// --- Admin Operating Hours ---
export const adminOperatingHoursApi = {
  getAll: () => client.get('/admin/operating-hours'),
  getByDay: (day) => client.get(`/admin/operating-hours/${day}`),
  update: (day, data) => client.put(`/admin/operating-hours/${day}`, data),
};

// --- Coupons (public) ---
export const couponApi = {
  validate: (code) => client.get('/coupons/validate', { params: { code } }),
};

// --- WebSocket ---
export const WS_BASE = import.meta.env.VITE_WS_URL || 
  (window.location.protocol === 'https:' ? 'wss://' : 'ws://') + 
  window.location.host + '/ws';
