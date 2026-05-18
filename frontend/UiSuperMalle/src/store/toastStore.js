import { create } from 'zustand';
import { v4 as uuidv4 } from 'uuid';

// Toast types
export const TOAST_TYPES = {
  SUCCESS: 'success',
  ERROR: 'error',
  WARNING: 'warning',
  INFO: 'info',
};

// Toast duration (ms)
const DURATION = {
  SHORT: 3000,
  MEDIUM: 5000,
  LONG: 8000,
  PERSISTENT: null,
};

const useToastStore = create((set, get) => ({
  toasts: [],

  addToast: (message, type = TOAST_TYPES.INFO, duration = DURATION.MEDIUM) => {
    const id = uuidv4();
    const toast = {
      id,
      message,
      type,
      duration,
      createdAt: new Date(),
    };

    set((state) => ({
      toasts: [...state.toasts, toast],
    }));

    // Auto-remove if duration is set
    if (duration) {
      setTimeout(() => {
        get().removeToast(id);
      }, duration);
    }

    return id;
  },

  removeToast: (id) => {
    set((state) => ({
      toasts: state.toasts.filter((toast) => toast.id !== id),
    }));
  },

  clearAll: () => {
    set({ toasts: [] });
  },

  // Convenience methods
  success: (message, duration = DURATION.MEDIUM) => {
    return get().addToast(message, TOAST_TYPES.SUCCESS, duration);
  },

  error: (message, duration = DURATION.LONG) => {
    return get().addToast(message, TOAST_TYPES.ERROR, duration);
  },

  warning: (message, duration = DURATION.MEDIUM) => {
    return get().addToast(message, TOAST_TYPES.WARNING, duration);
  },

  info: (message, duration = DURATION.MEDIUM) => {
    return get().addToast(message, TOAST_TYPES.INFO, duration);
  },
}));

export default useToastStore;
