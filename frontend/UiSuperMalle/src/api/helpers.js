/**
 * Helpers for working with backend API responses.
 * 
 * Backend PagedResponse<T> shape:
 *   { items: T[], total, page, size, totalPages }
 * 
 * Backend returns list endpoints directly or wrapped in PagedResponse.
 */

/**
 * Extract items from a paginated or plain API response.
 * Handles both PagedResponse (items[]) and plain arrays.
 */
export function extractItems(res) {
  const data = res.data;
  if (Array.isArray(data)) return data;
  if (data?.items && Array.isArray(data.items)) return data.items;
  if (data?.content && Array.isArray(data.content)) return data.content; // fallback
  return [];
}

/**
 * Extract paginated response with metadata.
 * Returns { items, total, page, size, totalPages }
 */
export function extractPage(res) {
  const data = res.data;
  if (data?.items) {
    return {
      items: data.items,
      total: data.totalElements ?? data.total ?? 0,
      page: data.pageNumber ?? data.page ?? 0,
      size: data.pageSize ?? data.size ?? 20,
      totalPages: data.totalPages ?? 1,
      last: data.last ?? false,
    };
  }
  // Fallback for non-paginated responses
  if (Array.isArray(data)) {
    return { items: data, total: data.length, page: 0, size: data.length, totalPages: 1, last: true };
  }
  return { items: [], total: 0, page: 0, size: 20, totalPages: 0, last: true };
}

/**
 * Format a price value as USD currency.
 * Backend returns values in dollars (BigDecimal), NOT cents.
 */
export function formatPrice(value) {
  const num = typeof value === 'string' ? parseFloat(value) : Number(value);
  if (isNaN(num)) return '$0.00';
  
  return num.toLocaleString('en-US', { 
    style: 'currency', 
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
}

/**
 * Convert dollar amount to cents for backend API calls.
 */


/**
 * Format a date string (ISO) to a human-readable format.
 */
export function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

/**
 * Order status color mapping (matches backend enum).
 */
export const STATUS_CONFIG = {
  PENDING:         { bg: 'bg-yellow-100',  text: 'text-yellow-800',  dot: 'bg-yellow-500' },
  CONFIRMED:       { bg: 'bg-blue-100',    text: 'text-blue-800',    dot: 'bg-blue-500' },
  PREPARING:       { bg: 'bg-orange-100',  text: 'text-orange-800',  dot: 'bg-orange-500' },
  READY:           { bg: 'bg-emerald-100', text: 'text-emerald-800', dot: 'bg-emerald-500' },
  DELIVERED:       { bg: 'bg-green-100',   text: 'text-green-800',   dot: 'bg-green-500' },
  CANCELLED:       { bg: 'bg-red-100',     text: 'text-red-800',     dot: 'bg-red-500' },
  // Legacy aliases for backward compat
  ACCEPTED:        { bg: 'bg-blue-100',    text: 'text-blue-800',    dot: 'bg-blue-500' },
  OUT_FOR_DELIVERY:{ bg: 'bg-indigo-100',  text: 'text-indigo-800',  dot: 'bg-indigo-500' },
  COMPLETED:       { bg: 'bg-green-100',   text: 'text-green-800',   dot: 'bg-green-500' },
};

/**
 * Get status display config, with fallback for lowercase statuses.
 */
export function getStatusConfig(status) {
  const key = (status || '').toUpperCase();
  return STATUS_CONFIG[key] || { bg: 'bg-gray-100', text: 'text-gray-800', dot: 'bg-gray-500' };
}

/**
 * Payment status config.
 */
export const PAYMENT_STATUS_CONFIG = {
  PENDING:         { bg: 'bg-yellow-100',  text: 'text-yellow-800' },
  REQUIRES_PAYMENT_METHOD: { bg: 'bg-yellow-100', text: 'text-yellow-800' },
  REQUIRES_CONFIRMATION:   { bg: 'bg-yellow-100', text: 'text-yellow-800' },
  PROCESSING:      { bg: 'bg-blue-100',    text: 'text-blue-800' },
  SUCCEEDED:       { bg: 'bg-green-100',   text: 'text-green-800' },
  COMPLETED:       { bg: 'bg-green-100',   text: 'text-green-800' },
  FAILED:          { bg: 'bg-red-100',     text: 'text-red-800' },
  CANCELED:        { bg: 'bg-gray-100',    text: 'text-gray-800' },
  REFUNDED:        { bg: 'bg-purple-100',   text: 'text-purple-800' },
  PARTIALLY_REFUNDED: { bg: 'bg-purple-100', text: 'text-purple-800' },
};

export function getPaymentStatusConfig(status) {
  const key = (status || '').toUpperCase();
  return PAYMENT_STATUS_CONFIG[key] || { bg: 'bg-gray-100', text: 'text-gray-800' };
}
