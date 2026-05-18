import { useState, useEffect, useCallback } from 'react';
import {
  Search,
  Filter,
  ChevronLeft,
  ChevronRight,
  Eye,
  X,
  Clock,
  MapPin,
  Package,
  RefreshCw,
  CheckCircle2,
  Circle,
  AlertCircle,
  Truck,
  UtensilsCrossed,
  Ban,
  CreditCard,
} from 'lucide-react';
import { adminOrderApi } from '../../api/endpoints';
import {
  formatPrice,
  formatDate,
  getStatusConfig,
  getPaymentStatusConfig,
  extractPage,
} from '../../api/helpers';

/* ──────────────────── Constants (aligned with backend OrderStatus enum) ──────────────────── */

const STATUSES = [
  'PENDING',
  'CONFIRMED',
  'PREPARING',
  'READY',
  'DELIVERED',
  'CANCELLED',
];

// Status flow: which statuses can be transitioned to from the current one
const NEXT_STATUS_MAP = {
  PENDING:   ['CONFIRMED', 'CANCELLED'],
  CONFIRMED: ['PREPARING', 'CANCELLED'],
  PREPARING: ['READY', 'CANCELLED'],
  READY:     ['DELIVERED', 'CANCELLED'],
  DELIVERED: [],
  CANCELLED: [],
};

const STATUS_ICON = {
  PENDING:   Clock,
  CONFIRMED: CheckCircle2,
  PREPARING: UtensilsCrossed,
  READY:     Package,
  DELIVERED: Truck,
  CANCELLED: AlertCircle,
};

const PAGE_SIZE = 20;

/* ──────────────────── Helpers ──────────────────── */

function prettyStatus(s) {
  if (!s) return '';
  return s.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase());
}

/* ──────────────────── Skeleton ──────────────────── */

function TableSkeleton() {
  return (
    <div className="bg-bg-surface rounded-xl shadow-copper-md overflow-hidden">
      <div className="divide-y divide-border-subtle">
        {Array.from({ length: 8 }).map((_, i) => (
          <div key={i} className="px-6 py-4 flex items-center gap-4">
            <div className="animate-shimmer h-4 bg-bg-hover rounded w-20" />
            <div className="animate-shimmer h-4 bg-bg-hover rounded w-24" />
            <div className="animate-shimmer h-4 bg-bg-hover rounded w-16" />
            <div className="animate-shimmer h-6 bg-bg-hover rounded-full w-20" />
            <div className="animate-shimmer h-4 bg-bg-hover rounded w-16" />
            <div className="animate-shimmer h-4 bg-bg-hover rounded w-28" />
            <div className="ml-auto animate-shimmer h-8 bg-bg-hover rounded-lg w-8" />
          </div>
        ))}
      </div>
    </div>
  );
}

/* ──────────────────── Status Badge (using helpers) ──────────────────── */

function StatusBadge({ status }) {
  const cfg = getStatusConfig(status);
  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold ring-1 ring-inset ${cfg.bg} ${cfg.text}`}
    >
      <span className={`w-1.5 h-1.5 rounded-full ${cfg.dot}`} />
      {prettyStatus(status)}
    </span>
  );
}

/* ──────────────────── Payment Status Badge ──────────────────── */

function PaymentStatusBadge({ status }) {
  const cfg = getPaymentStatusConfig(status);
  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold ${cfg.bg} ${cfg.text}`}
    >
      <CreditCard className="w-3 h-3" />
      {prettyStatus(status)}
    </span>
  );
}

/* ──────────────────── Error Banner ──────────────────── */

function ErrorBanner({ message }) {
  return (
    <div className="flex items-center gap-3 bg-red-50 border border-red-200 text-red-700 rounded-xl px-5 py-4">
      <AlertCircle className="w-5 h-5 shrink-0" />
      <p className="text-sm">{message}</p>
    </div>
  );
}

/* ──────────────────── Status Timeline ──────────────────── */

function StatusTimeline({ statusLog }) {
  if (!statusLog || statusLog.length === 0) {
    return (
      <p className="text-sm text-text-dim py-4 text-center">No status history available</p>
    );
  }

  const sorted = [...statusLog].sort(
    (a, b) => new Date(b.timestamp) - new Date(a.timestamp),
  );

  return (
    <div className="relative">
      {sorted.map((entry, idx) => {
        const Icon = STATUS_ICON[entry.status] || Circle;
        const isLatest = idx === 0;
        return (
          <div key={idx} className="flex gap-3 pb-5 last:pb-0">
            {/* timeline line + dot */}
            <div className="flex flex-col items-center">
              <div
                className={`flex items-center justify-center w-8 h-8 rounded-full shrink-0 ${
                  isLatest
                    ? 'bg-copper-500/20 text-copper-500'
                    : 'bg-bg-hover text-text-dim'
                }`}
              >
                <Icon className="w-4 h-4" />
              </div>
              {idx < sorted.length - 1 && (
                <div className="w-px flex-1 bg-border-subtle mt-1" />
              )}
            </div>
            {/* content */}
            <div className="pt-1 min-w-0">
              <div className="flex items-center gap-2">
                <StatusBadge status={entry.status} />
              </div>
              {entry.note && (
                <p className="text-sm text-text-secondary mt-0.5">{entry.note}</p>
              )}
              <p className="text-xs text-text-dim mt-0.5">
                {formatDate(entry.timestamp)}
              </p>
            </div>
          </div>
        );
      })}
    </div>
  );
}

/* ──────────────────── Cancel Dialog ──────────────────── */

function CancelDialog({ orderId, onClose, onCancelled }) {
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleCancel = async () => {
    setSubmitting(true);
    setError(null);
    try {
      await adminOrderApi.cancel(orderId, reason || 'Cancelled by admin');
      onCancelled();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to cancel order.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-bg-surface rounded-xl shadow-copper-md w-full max-w-md p-6 space-y-4">
        <div className="flex items-center gap-3">
          <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-red-100">
            <Ban className="w-5 h-5 text-red-600" />
          </div>
          <div>
            <h3 className="text-lg font-bold text-text-primary">Cancel Order</h3>
            <p className="text-sm text-text-secondary">This action cannot be undone.</p>
          </div>
        </div>

        {error && <ErrorBanner message={error} />}

        <div>
          <label className="block text-sm font-medium text-text-primary mb-1">
            Reason (optional)
          </label>
          <textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="Why is this order being cancelled?"
            rows={3}
            className="w-full rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-red-500 focus:ring-1 focus:ring-red-500 outline-none transition resize-none"
          />
        </div>

        <div className="flex items-center justify-end gap-3 pt-2">
          <button
            onClick={onClose}
            className="px-4 py-2.5 rounded-xl border border-border-subtle text-sm font-medium text-text-secondary hover:bg-bg-hover transition"
          >
            Go Back
          </button>
          <button
            onClick={handleCancel}
            disabled={submitting}
            className="px-4 py-2.5 bg-gradient-to-r from-red-500 to-red-600 text-white text-sm font-semibold rounded-xl hover:from-red-600 hover:to-red-700 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
          >
            {submitting && <RefreshCw className="w-4 h-4 animate-spin" />}
            Cancel Order
          </button>
        </div>
      </div>
    </div>
  );
}

/* ──────────────────── Order Detail Modal ──────────────────── */

function OrderDetailModal({ order, onClose, onStatusUpdated }) {
  const [newStatus, setNewStatus] = useState('');
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState(null);
  const [showCancelDialog, setShowCancelDialog] = useState(false);

  if (!order) return null;

  const nextStatuses = NEXT_STATUS_MAP[order.status] || [];

  const handleUpdateStatus = async (status) => {
    const targetStatus = status || newStatus;
    if (!targetStatus) return;
    setUpdating(true);
    setError(null);
    try {
      await adminOrderApi.updateStatus(order.id, targetStatus);
      setNewStatus('');
      onStatusUpdated();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to update order status.');
    } finally {
      setUpdating(false);
    }
  };

  const handleCancelSuccess = () => {
    setShowCancelDialog(false);
    onStatusUpdated();
  };

  const isDelivery = order.orderType === 'DELIVERY';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/40 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* Modal */}
      <div className="relative bg-bg-surface rounded-xl shadow-copper-md w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="sticky top-0 bg-bg-surface z-10 flex items-center justify-between px-6 py-4 border-b border-border-subtle rounded-t-xl">
          <div className="flex items-center gap-3">
            <h2 className="text-lg font-bold text-text-primary">
              Order #{order.orderNumber}
            </h2>
            <StatusBadge status={order.status} />
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg hover:bg-bg-hover transition text-text-dim hover:text-text-secondary"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="px-6 py-5 space-y-6">
          {/* Error */}
          {error && <ErrorBanner message={error} />}

          {/* Order info grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <div>
              <p className="text-xs font-medium text-text-dim uppercase tracking-wide">Type</p>
              <p className="text-sm font-semibold text-text-primary mt-0.5 capitalize">
                {prettyStatus(order.orderType)}
              </p>
            </div>
            <div>
              <p className="text-xs font-medium text-text-dim uppercase tracking-wide">Payment</p>
              <p className="text-sm font-semibold text-text-primary mt-0.5">
                {order.paymentMethod || '--'}
              </p>
              <div className="mt-0.5">
                <PaymentStatusBadge status={order.paymentStatus} />
              </div>
            </div>
            <div>
              <p className="text-xs font-medium text-text-dim uppercase tracking-wide">Total</p>
              <p className="text-sm font-bold text-text-primary mt-0.5">
                {formatPrice(order.finalTotal ?? order.total)}
              </p>
              {order.discountAmount > 0 && (
                <p className="text-xs text-text-dim line-through">
                  {formatPrice(order.total)}
                </p>
              )}
            </div>
            <div>
              <p className="text-xs font-medium text-text-dim uppercase tracking-wide">Created</p>
              <p className="text-sm font-semibold text-text-primary mt-0.5">
                {formatDate(order.createdAt)}
              </p>
            </div>
          </div>

          {/* Delivery Address */}
          {isDelivery && order.deliveryAddress && (
            <div className="bg-bg-hover rounded-xl p-4">
              <div className="flex items-center gap-2 text-sm font-medium text-text-primary mb-1">
                <MapPin className="w-4 h-4 text-copper-500" />
                Delivery Address
              </div>
              <p className="text-sm text-text-secondary pl-6">{order.deliveryAddress}</p>
              {order.estimatedDeliveryTime && (
                <p className="text-xs text-text-dim pl-6 mt-0.5">
                  Est. delivery: {formatDate(order.estimatedDeliveryTime)}
                </p>
              )}
            </div>
          )}

          {/* Special Instructions / Coupon */}
          {(order.specialInstructions || order.couponCode) && (
            <div className="flex flex-wrap gap-3">
              {order.specialInstructions && (
                <div className="bg-copper-500/10 border border-copper-500/20 rounded-xl px-4 py-2.5 text-sm text-copper-500">
                  <span className="font-medium">Note:</span> {order.specialInstructions}
                </div>
              )}
              {order.couponCode && (
                <div className="bg-emerald-500/10 border border-emerald-500/20 rounded-xl px-4 py-2.5 text-sm text-emerald-400">
                  <span className="font-medium">Coupon:</span> {order.couponCode}
                  {order.discountAmount > 0 && ` (-${formatPrice(order.discountAmount)})`}
                </div>
              )}
            </div>
          )}

          {/* Cancellation Reason */}
          {order.status === 'CANCELLED' && order.cancellationReason && (
            <div className="bg-red-50 border border-red-200 rounded-xl px-4 py-2.5 text-sm text-red-800">
              <span className="font-medium">Cancellation reason:</span> {order.cancellationReason}
            </div>
          )}

          {/* Order Items */}
          <div>
            <h3 className="text-sm font-semibold text-text-primary mb-3">Order Items</h3>
            <div className="border border-border-subtle rounded-xl overflow-hidden">
              <table className="w-full text-sm">
                <thead>
                  <tr className="bg-bg-hover text-text-secondary text-xs uppercase tracking-wide">
                    <th className="text-left px-4 py-2.5 font-medium">Item</th>
                    <th className="text-center px-3 py-2.5 font-medium">Qty</th>
                    <th className="text-right px-3 py-2.5 font-medium">Price</th>
                    <th className="text-right px-4 py-2.5 font-medium">Subtotal</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border-subtle">
                  {order.items?.map((item, idx) => (
                    <tr key={item.id || idx} className="hover:bg-bg-hover/50">
                      <td className="px-4 py-3">
                        <p className="font-medium text-text-primary">{item.menuItemName}</p>
                        {item.customizations && (
                          <p className="text-xs text-text-dim mt-0.5">
                            {typeof item.customizations === 'string'
                              ? item.customizations
                              : JSON.stringify(item.customizations)}
                          </p>
                        )}
                      </td>
                      <td className="text-center px-3 py-3 text-text-secondary">{item.quantity}</td>
                      <td className="text-right px-3 py-3 text-text-secondary">
                        {formatPrice(item.unitPrice)}
                      </td>
                      <td className="text-right px-4 py-3 font-medium text-text-primary">
                        {formatPrice(item.subtotal)}
                      </td>
                    </tr>
                  ))}
                </tbody>
                <tfoot>
                  {order.discountAmount > 0 && (
                    <tr className="bg-bg-hover">
                      <td colSpan={3} className="text-right px-4 py-2 text-sm text-text-secondary">
                        Subtotal
                      </td>
                      <td className="text-right px-4 py-2 text-sm text-text-primary">
                        {formatPrice(order.subtotal)}
                      </td>
                    </tr>
                  )}
                  {order.discountAmount > 0 && (
                    <tr className="bg-bg-hover">
                      <td colSpan={3} className="text-right px-4 py-2 text-sm text-green-600">
                        Discount
                      </td>
                      <td className="text-right px-4 py-2 text-sm text-green-600">
                        -{formatPrice(order.discountAmount)}
                      </td>
                    </tr>
                  )}
                  {order.tax > 0 && (
                    <tr className="bg-bg-hover">
                      <td colSpan={3} className="text-right px-4 py-2 text-sm text-text-secondary">
                        Tax
                      </td>
                      <td className="text-right px-4 py-2 text-sm text-text-primary">
                        {formatPrice(order.tax)}
                      </td>
                    </tr>
                  )}
                  <tr className="bg-bg-hover">
                    <td colSpan={3} className="text-right px-4 py-3 text-sm font-semibold text-text-primary">
                      Total
                    </td>
                    <td className="text-right px-4 py-3 text-sm font-bold text-text-primary">
                      {formatPrice(order.finalTotal ?? order.total)}
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>

          {/* Status Timeline */}
          <div>
            <h3 className="text-sm font-semibold text-text-primary mb-3">Status History</h3>
            <StatusTimeline statusLog={order.statusLog} />
          </div>

          {/* Update Status - Quick Action Buttons */}
          {nextStatuses.length > 0 && order.status !== 'CANCELLED' && (
            <div className="bg-bg-hover rounded-xl p-4">
              <h3 className="text-sm font-semibold text-text-primary mb-3">Update Status</h3>
              <div className="flex flex-wrap gap-2">
                {nextStatuses
                  .filter((s) => s !== 'CANCELLED')
                  .map((s) => {
                    const Icon = STATUS_ICON[s] || CheckCircle2;
                    const cfg = getStatusConfig(s);
                    return (
                      <button
                        key={s}
                        onClick={() => handleUpdateStatus(s)}
                        disabled={updating}
                        className={`inline-flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm font-semibold transition disabled:opacity-50 disabled:cursor-not-allowed ${cfg.bg} ${cfg.text} hover:opacity-80`}
                      >
                        {updating ? (
                          <RefreshCw className="w-4 h-4 animate-spin" />
                        ) : (
                          <Icon className="w-4 h-4" />
                        )}
                        {prettyStatus(s)}
                      </button>
                    );
                  })}
              </div>
            </div>
          )}

          {/* Cancel Button */}
          {order.status !== 'CANCELLED' && order.status !== 'DELIVERED' && (
            <div className="flex justify-end">
              <button
                onClick={() => setShowCancelDialog(true)}
                className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl border border-red-200 text-sm font-semibold text-red-600 hover:bg-red-50 hover:border-red-300 transition"
              >
                <Ban className="w-4 h-4" />
                Cancel Order
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Cancel Dialog */}
      {showCancelDialog && (
        <CancelDialog
          orderId={order.id}
          onClose={() => setShowCancelDialog(false)}
          onCancelled={handleCancelSuccess}
        />
      )}
    </div>
  );
}

/* ──────────────────── Main Page ──────────────────── */

export default function AdminOrders() {
  // Filters
  const [statusFilter, setStatusFilter] = useState('');
  const [search, setSearch] = useState('');

  // Data
  const [orders, setOrders] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Detail modal
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);

  /* ── Fetch orders ── */

  const fetchOrders = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { page, size: PAGE_SIZE, sort: 'createdAt,desc' };
      if (statusFilter) params.status = statusFilter;
      const res = await adminOrderApi.list(params);
      const pageData = extractPage(res);
      setOrders(pageData.items);
      setTotalPages(pageData.totalPages);
      setTotalElements(pageData.total);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load orders.');
      setOrders([]);
    } finally {
      setLoading(false);
    }
  }, [page, statusFilter]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  /* ── Open detail ── */

  const openDetail = async (order) => {
    setSelectedOrder(order);
    setDetailLoading(true);
    try {
      const res = await adminOrderApi.get(order.id);
      setSelectedOrder(res.data);
    } catch {
      // fallback to list data
    } finally {
      setDetailLoading(false);
    }
  };

  /* ── After status update ── */

  const handleStatusUpdated = async () => {
    // Refresh the detail
    if (selectedOrder) {
      try {
        const res = await adminOrderApi.get(selectedOrder.id);
        setSelectedOrder(res.data);
      } catch {
        // ignore
      }
    }
    // Refresh the list
    fetchOrders();
  };

  /* ── Filtered orders (client-side search on orderNumber) ── */

  const displayedOrders = search
    ? orders.filter(
        (o) =>
          o.orderNumber?.toLowerCase().includes(search.toLowerCase()) ||
          o.id?.toString().includes(search),
      )
    : orders;

  return (
    <div className="min-h-screen bg-bg-base animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        {/* ── Header ── */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-text-primary">Orders</h1>
            <p className="mt-1 text-text-secondary">
              Manage and track all restaurant orders
            </p>
          </div>
          <button
            onClick={fetchOrders}
            className="p-2.5 rounded-xl bg-bg-surface border border-border-subtle text-text-secondary hover:text-copper-500 hover:border-copper-500/30 hover:bg-copper-500/10 transition shadow-copper-sm"
            title="Refresh"
          >
            <RefreshCw className="w-5 h-5" />
          </button>
        </div>

        {/* ── Error ── */}
        {error && <ErrorBanner message={error} />}

        {/* ── Filter Bar ── */}
        <div className="bg-bg-surface rounded-xl shadow-copper-md p-4">
          <div className="flex flex-col lg:flex-row items-stretch lg:items-center gap-3">
            {/* Search */}
            <div className="relative flex-1 min-w-0">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim" />
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search by order #..."
                className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-border-subtle text-sm text-text-primary placeholder-text-dim focus:border-copper-500 focus:ring-1 focus:ring-copper-500 outline-none transition"
              />
            </div>

            {/* Status filter */}
            <div className="relative">
              <Filter className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim pointer-events-none" />
              <select
                value={statusFilter}
                onChange={(e) => {
                  setStatusFilter(e.target.value);
                  setPage(0);
                }}
                className="pl-10 pr-8 py-2.5 rounded-xl border border-border-subtle text-sm text-text-primary bg-bg-surface focus:border-copper-500 focus:ring-1 focus:ring-copper-500 outline-none transition appearance-none cursor-pointer"
              >
                <option value="">All Statuses</option>
                {STATUSES.map((s) => (
                  <option key={s} value={s}>
                    {prettyStatus(s)}
                  </option>
                ))}
              </select>
            </div>

            {/* Clear */}
            {(statusFilter || search) && (
              <button
                onClick={() => {
                  setStatusFilter('');
                  setSearch('');
                  setPage(0);
                }}
                className="px-4 py-2.5 rounded-xl border border-border-subtle text-sm font-medium text-text-secondary hover:text-red-600 hover:border-red-200 hover:bg-red-50 transition"
              >
                Clear
              </button>
            )}
          </div>
        </div>

        {/* ── Results count ── */}
        <div className="flex items-center justify-between">
          <p className="text-sm text-text-secondary">
            {loading ? 'Loading...' : `${totalElements} order${totalElements !== 1 ? 's' : ''} found`}
          </p>
        </div>

        {/* ── Orders Table ── */}
        {loading ? (
          <TableSkeleton />
        ) : displayedOrders.length === 0 ? (
          <div className="bg-bg-surface rounded-xl shadow-copper-md py-20 flex flex-col items-center justify-center text-text-dim">
            <Package className="w-14 h-14 text-text-dim mb-3" />
            <p className="text-lg font-semibold text-text-secondary">No orders found</p>
            <p className="text-sm mt-1">Try adjusting your filters</p>
          </div>
        ) : (
          <div className="bg-bg-surface rounded-xl shadow-copper-md overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="bg-bg-hover border-b border-border-subtle">
                    <th className="text-left px-6 py-3.5 text-xs font-semibold text-text-secondary uppercase tracking-wider">
                      Order #
                    </th>
                    <th className="text-left px-4 py-3.5 text-xs font-semibold text-text-secondary uppercase tracking-wider">
                      Type
                    </th>
                    <th className="text-center px-4 py-3.5 text-xs font-semibold text-text-secondary uppercase tracking-wider">
                      Items
                    </th>
                    <th className="text-right px-4 py-3.5 text-xs font-semibold text-text-secondary uppercase tracking-wider">
                      Total
                    </th>
                    <th className="text-left px-4 py-3.5 text-xs font-semibold text-text-secondary uppercase tracking-wider">
                      Status
                    </th>
                    <th className="text-left px-4 py-3.5 text-xs font-semibold text-text-secondary uppercase tracking-wider">
                      Payment
                    </th>
                    <th className="text-left px-4 py-3.5 text-xs font-semibold text-text-secondary uppercase tracking-wider">
                      Date
                    </th>
                    <th className="text-right px-6 py-3.5 text-xs font-semibold text-text-secondary uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border-subtle">
                  {displayedOrders.map((order) => (
                    <tr
                      key={order.id}
                      className="hover:bg-copper-500/5 transition-colors cursor-pointer"
                      onClick={() => openDetail(order)}
                    >
                      <td className="px-6 py-4">
                        <span className="font-mono font-semibold text-text-primary">
                          #{order.orderNumber}
                        </span>
                      </td>
                      <td className="px-4 py-4">
                        <span className="inline-flex items-center gap-1.5 text-text-primary capitalize">
                          {order.orderType === 'DELIVERY' ? (
                            <Truck className="w-3.5 h-3.5 text-purple-500" />
                          ) : (
                            <UtensilsCrossed className="w-3.5 h-3.5 text-copper-500" />
                          )}
                          {prettyStatus(order.orderType)}
                        </span>
                      </td>
                      <td className="text-center px-4 py-4 text-text-secondary">
                        {order.items?.length ?? '--'}
                      </td>
                      <td className="px-4 py-4 text-right font-semibold text-text-primary">
                        {formatPrice(order.finalTotal ?? order.total)}
                      </td>
                      <td className="px-4 py-4">
                        <StatusBadge status={order.status} />
                      </td>
                      <td className="px-4 py-4">
                        <PaymentStatusBadge status={order.paymentStatus} />
                      </td>
                      <td className="px-4 py-4 text-text-secondary">
                        {formatDate(order.createdAt)}
                      </td>
                      <td className="px-6 py-4 text-right">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            openDetail(order);
                          }}
                          className="p-2 rounded-lg text-text-dim hover:text-copper-500 hover:bg-copper-500/20 transition"
                          title="View details"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ── Pagination ── */}
        {totalPages > 1 && (
          <div className="flex items-center justify-center gap-2">
            <button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
              className="inline-flex items-center gap-1 px-3.5 py-2 rounded-xl border border-border-subtle text-sm font-medium text-text-secondary hover:bg-copper-500/10 hover:border-copper-500/30 hover:text-copper-500 transition disabled:opacity-40 disabled:cursor-not-allowed"
            >
              <ChevronLeft className="w-4 h-4" />
              Prev
            </button>

            {/* Page numbers */}
            <div className="flex items-center gap-1">
              {(() => {
                const pages = [];
                const maxVisible = 5;
                let start = Math.max(0, page - Math.floor(maxVisible / 2));
                let end = Math.min(totalPages, start + maxVisible);
                if (end - start < maxVisible) {
                  start = Math.max(0, end - maxVisible);
                }
                for (let i = start; i < end; i++) {
                  pages.push(i);
                }
                return pages.map((i) => (
                  <button
                    key={i}
                    onClick={() => setPage(i)}
                    className={`w-9 h-9 rounded-xl text-sm font-semibold transition ${
                      page === i
                        ? 'bg-copper-500 text-white shadow-copper-sm'
                        : 'text-text-secondary hover:bg-copper-500/10 hover:text-copper-500'
                    }`}
                  >
                    {i + 1}
                  </button>
                ));
              })()}
            </div>

            <button
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="inline-flex items-center gap-1 px-3.5 py-2 rounded-xl border border-border-subtle text-sm font-medium text-text-secondary hover:bg-copper-500/10 hover:border-copper-500/30 hover:text-copper-500 transition disabled:opacity-40 disabled:cursor-not-allowed"
            >
              Next
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        )}
      </div>

      {/* ── Detail Modal ── */}
      {selectedOrder && (
        <OrderDetailModal
          order={selectedOrder}
          onClose={() => setSelectedOrder(null)}
          onStatusUpdated={handleStatusUpdated}
        />
      )}
    </div>
  );
}
