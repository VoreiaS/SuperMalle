import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL || '/api/v1';

// Request timeout (ms)
const REQUEST_TIMEOUT = 30000;

// Retry configuration
const MAX_RETRIES = 3;
const RETRY_DELAY = 1000;

// Retryable status codes
const RETRYABLE_STATUS_CODES = [408, 429, 500, 502, 503, 504];

// Create axios instance
const client = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
  timeout: REQUEST_TIMEOUT,
});

// Helper function to delay
const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

// Helper function to check if error is retryable
const isRetryable = (error) => {
  if (!error.response) return true; // Network errors are retryable
  return RETRYABLE_STATUS_CODES.includes(error.response.status);
};

// Request interceptor
client.interceptors.request.use(
  (config) => {
    // Add JWT token to headers
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Add request ID for tracking
    config.metadata = { requestId: crypto.randomUUID() };

    // Log request in development
    if (import.meta.env.DEV) {
      console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, {
        data: config.data,
        headers: config.headers,
      });
    }

    return config;
  },
  (error) => {
    console.error('[API Request Error]', error);
    return Promise.reject(error);
  }
);

// Response interceptor with retry logic
client.interceptors.response.use(
  (response) => {
    // Log response in development
    if (import.meta.env.DEV) {
      console.log(`[API Response] ${response.config.method?.toUpperCase()} ${response.config.url}`, {
        status: response.status,
        data: response.data,
      });
    }

    return response;
  },
  async (error) => {
    const config = error.config;

    // Log error in development
    if (import.meta.env.DEV) {
      console.error('[API Response Error]', {
        url: config?.url,
        method: config?.method,
        status: error.response?.status,
        message: error.message,
        data: error.response?.data,
      });
    }

    // Handle 401 Unauthorized - auto logout
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');

      // Only redirect if not already on login page
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }

      return Promise.reject(error);
    }

    // Retry logic
    if (!config._retry && isRetryable(error) && MAX_RETRIES > 0) {
      config._retry = true;
      config._retryCount = (config._retryCount || 0) + 1;

      if (config._retryCount <= MAX_RETRIES) {
        console.log(
          `[API Retry] Attempt ${config._retryCount}/${MAX_RETRIES} for ${config.method?.toUpperCase()} ${config.url}`
        );

        // Exponential backoff
        const retryDelay = RETRY_DELAY * Math.pow(2, config._retryCount - 1);
        await delay(retryDelay);

        return client(config);
      }
    }

    return Promise.reject(error);
  }
);

// Helper function to create a cancellable request
export function createCancellableRequest(requestFn) {
  const controller = new AbortController();

  const promise = requestFn({
    signal: controller.signal,
  });

  return {
    promise,
    cancel: () => controller.abort(),
  };
}

// Helper function to handle API errors consistently
export function handleApiError(error) {
  if (error.response) {
    // Server responded with error status
    const { status, data } = error.response;

    switch (status) {
      case 400:
        return data?.message || 'Invalid request. Please check your input.';
      case 401:
        return 'Authentication required. Please log in.';
      case 403:
        return 'You do not have permission to perform this action.';
      case 404:
        return 'The requested resource was not found.';
      case 409:
        return data?.message || 'This action conflicts with existing data.';
      case 422:
        return data?.message || 'Validation failed. Please check your input.';
      case 429:
        return 'Too many requests. Please try again later.';
      case 500:
        return 'Server error. Please try again later.';
      case 503:
        return 'Service unavailable. Please try again later.';
      default:
        return data?.message || 'An unexpected error occurred.';
    }
  } else if (error.request) {
    // Request made but no response received
    return 'Network error. Please check your connection and try again.';
  } else {
    // Error in request configuration
    return error.message || 'An unexpected error occurred.';
  }
}

// Helper function to extract error message from API response
export function extractErrorMessage(error) {
  if (error?.response?.data?.message) {
    return error.response.data.message;
  }
  if (error?.response?.data?.error) {
    return error.response.data.error;
  }
  if (error?.message) {
    return error.message;
  }
  return 'An unexpected error occurred.';
}

export default client;
