import { create } from 'zustand';
import { cartApi } from '../api/endpoints';
import { handleApiError } from '../api/client';

const useCartStore = create((set, get) => ({
  cart: null,
  loading: false,
  itemCount: 0,
  error: null,

  // Calculate item count from cart
  _calculateItemCount: (cart) => {
    return cart?.items?.reduce((sum, item) => sum + item.quantity, 0) || 0;
  },

  fetchCart: async () => {
    set({ loading: true, error: null });
    try {
      const res = await cartApi.get();
      const cart = res.data;
      const itemCount = get()._calculateItemCount(cart);
      set({ cart, itemCount, loading: false });
      return cart;
    } catch (err) {
      const msg = handleApiError(err);
      set({ cart: null, itemCount: 0, loading: false, error: msg });
      throw err;
    }
  },

  addItem: async (data) => {
    set({ loading: true, error: null });
    try {
      const res = await cartApi.addItem(data);
      const cart = res.data;
      const itemCount = get()._calculateItemCount(cart);
      set({ cart, itemCount, loading: false });
      return cart;
    } catch (err) {
      const msg = handleApiError(err);
      set({ loading: false, error: msg });
      throw err;
    }
  },

  updateItem: async (itemId, data) => {
    set({ loading: true, error: null });
    try {
      const res = await cartApi.updateItem(itemId, data);
      const cart = res.data;
      const itemCount = get()._calculateItemCount(cart);
      set({ cart, itemCount, loading: false });
      return cart;
    } catch (err) {
      const msg = handleApiError(err);
      set({ loading: false, error: msg });
      throw err;
    }
  },

  removeItem: async (itemId) => {
    set({ loading: true, error: null });
    try {
      const res = await cartApi.removeItem(itemId);
      const cart = res.data;
      const itemCount = get()._calculateItemCount(cart);
      set({ cart, itemCount, loading: false });
      return cart;
    } catch (err) {
      const msg = handleApiError(err);
      set({ loading: false, error: msg });
      throw err;
    }
  },

  clearCart: async () => {
    set({ loading: true, error: null });
    try {
      await cartApi.clear();
      set({ cart: null, itemCount: 0, loading: false });
    } catch (err) {
      const msg = handleApiError(err);
      set({ loading: false, error: msg });
      throw err;
    }
  },

  // Optimistic update for quantity change
  optimisticUpdateItem: (itemId, newQuantity) => {
    const { cart } = get();
    if (!cart) return;

    const updatedItems = cart.items.map((item) =>
      item.id === itemId ? { ...item, quantity: newQuantity } : item
    );

    const updatedCart = {
      ...cart,
      items: updatedItems,
      subtotal: updatedItems.reduce((sum, item) => sum + item.subtotal, 0),
      total: updatedItems.reduce((sum, item) => sum + item.subtotal, 0) + (cart.tax || 0),
    };

    const itemCount = get()._calculateItemCount(updatedCart);
    set({ cart: updatedCart, itemCount });
  },

  // Revert optimistic update
  revertOptimisticUpdate: () => {
    const { cart } = get();
    if (!cart) return;
    // Re-fetch cart to get the actual state
    get().fetchCart();
  },

  // Get cart total
  getTotal: () => {
    const { cart } = get();
    return cart?.total || 0;
  },

  // Get cart subtotal
  getSubtotal: () => {
    const { cart } = get();
    return cart?.subtotal || 0;
  },

  // Get cart tax
  getTax: () => {
    const { cart } = get();
    return cart?.tax || 0;
  },

  // Check if cart is empty
  isEmpty: () => {
    const { cart } = get();
    return !cart || !cart.items || cart.items.length === 0;
  },

  // Get cart items
  getItems: () => {
    const { cart } = get();
    return cart?.items || [];
  },

  // Clear error
  clearError: () => set({ error: null }),
}));

export default useCartStore;
