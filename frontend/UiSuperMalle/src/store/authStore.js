import { create } from 'zustand';
import { authApi, userApi } from '../api/endpoints';
import client, { handleApiError } from '../api/client';

const useAuthStore = create((set, get) => ({
  user: JSON.parse(localStorage.getItem('user') || 'null'),
  token: localStorage.getItem('token') || null,
  loading: false,
  error: null,
  isRefreshing: false,

  isAdmin: () => get().user?.role === 'ADMIN',

  // Backend AuthResponse is FLAT: { token, userId, name, email, role }
  // We normalize it into a user object: { id, name, email, role }
  _normalizeAuthResponse: (data) => {
    const { token, userId, name, email, role } = data;
    return { token, user: { id: userId, name, email, role } };
  },

  // Set authentication state
  _setAuth: (token, user) => {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
    set({ user, token, error: null });
  },

  // Clear authentication state
  _clearAuth: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    set({ user: null, token: null, error: null });
  },

  login: async (credentials) => {
    set({ loading: true, error: null });
    try {
      const res = await authApi.login(credentials);
      const { token, user } = get()._normalizeAuthResponse(res.data);
      get()._setAuth(token, user);
      return user;
    } catch (err) {
      const msg = handleApiError(err);
      set({ loading: false, error: msg });
      throw err;
    } finally {
      set({ loading: false });
    }
  },

  register: async (data) => {
    set({ loading: true, error: null });
    try {
      const res = await authApi.register(data);
      const { token, user } = get()._normalizeAuthResponse(res.data);
      get()._setAuth(token, user);
      return user;
    } catch (err) {
      const msg = handleApiError(err);
      set({ loading: false, error: msg });
      throw err;
    } finally {
      set({ loading: false });
    }
  },

  logout: () => {
    get()._clearAuth();
  },

  // Refresh user profile from server
  refreshProfile: async () => {
    try {
      const res = await userApi.getMe();
      const { token, refreshToken, userId, name, email, role } = res.data;
      const userData = { id: userId, name, email, role };
      localStorage.setItem('user', JSON.stringify(userData));
      set({ user: userData });
    } catch (err) {
      console.error('Failed to refresh profile:', err);
      // Don't clear auth on profile refresh failure
    }
  },

  // Update user profile
  updateProfile: async (data) => {
    set({ loading: true, error: null });
    try {
      const res = await userApi.updateProfile(data);
      const { token, refreshToken, userId, name, email, role } = res.data;
      const updatedUser = { id: userId, name, email, role };
      localStorage.setItem('user', JSON.stringify(updatedUser));
      set({ user: updatedUser, loading: false });
      return updatedUser;
    } catch (err) {
      const msg = handleApiError(err);
      set({ loading: false, error: msg });
      throw err;
    }
  },

  // Change password
  changePassword: async (data) => {
    set({ loading: true, error: null });
    try {
      await userApi.updatePassword(data);
      set({ loading: false });
    } catch (err) {
      const msg = handleApiError(err);
      set({ loading: false, error: msg });
      throw err;
    }
  },

  // Set user (for OAuth callback)
  setUser: (user) => {
    localStorage.setItem('user', JSON.stringify(user));
    set({ user });
  },

  // Set token (for OAuth callback)
  setToken: (token) => {
    localStorage.setItem('token', token);
    set({ token });
  },

  // Clear error
  clearError: () => set({ error: null }),

  // Check if user is authenticated
  isAuthenticated: () => {
    const { token, user } = get();
    return !!(token && user);
  },

  // Get user display name
  getDisplayName: () => {
    const { user } = get();
    if (!user) return '';
    return user.name || user.email || 'User';
  },

  // Get user initials
  getInitials: () => {
    const { user } = get();
    if (!user) return '';
    const name = user.name || '';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
  },
}));

export default useAuthStore;
