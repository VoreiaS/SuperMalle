import { useState, useEffect, useCallback } from 'react';
import {
  CreditCard,
  Filter,
  ChevronLeft,
  ChevronRight,
  Eye,
  X,
  RefreshCw,
  AlertCircle,
  RotateCcw,
  Receipt,
  ExternalLink,
  DollarSign,
} from 'lucide-react';
import { adminPaymentApi } from '../../api/endpoints';
import {
  formatPrice,
  formatDate,
  getPaymentStatusConfig,
  extractPage,
} from '../../api/helpers';

/* ──────────────────── Constants ──────────────────── */

const STATUSES = [
  'PENDING',
  'PROCESSING',
  'SUCCEEDED',
  'FAILED',
  'REFUNDED',
  'PARTIALLY_REFUNDED',
];

const PAGE_SIZE = 20;

/* ──────────────────── Helpers ──────────────────── */

function fmtAmount(amount, currency) {
  if (amount == null) return '--';
  const num = typeof amount === 'string' ? parseFloat(amount) : Number(amount);
  if (isNaN(num)) return '--';
  const cur = (currency || 'USD').toUpperCase();
  try {
    return num.toLocaleString('en-US', { style: 'currency', currency: cur });
  } catch {
    return formatPrice(num);
  }
}

function prettyStatus(s) {
  if (!s) return '';
  return s.replace(/_/g, ' ');
}

function prettyBrand(brand) {
  if (!brand) return '';
  return brand.charAt(0).toUpperCase() + brand.slice(1).toLowerCase();
}

/* ──────────────────── Skeleton ──────────────────── */

function TableSkeleton() {
  return (
    <div className="bg-bg-surface rounded-xl shadow-copper-sm overflow-hidden">
      <div className="divide-y divide-border-subtle">
        {Array.from({ length: 8 }).map((_, i) => (
          <div key={i} className="px-6 py-4 flex items-center gap-4">
            <div className="animate-shimmer h-4 bg-bg-hover rounded w-20" />
            <div className="animate-shimmer h-4 bg-bg-hover rounded w-24" />
            <div className="animate-shimmer h-4 bg-bg-hover rounded w-16" />
            <div className="animate-shimmer h-4 bg-bg-hover rounded w-20" />
            <div className="animate-shimmer h-6 bg-bg-hover rounded-full w-24" />
            <div className="animate-shimmer h-4 bg-bg-hover rounded w-28" />
            <div className="ml-auto animate-shimmer h-8 bg-bg-hover rounded-lg w-8" />
          </div>
        ))}
      </div>
    </div>
  );
}

/* ──────────────────── Status Badge ──────────────────── */

function StatusBadge({ status }) {
  const cfg = getPaymentStatusConfig(status);
  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold ring-1 ring-inset ${cfg.bg} ${cfg.text} ring-current/20`}
    >
      {prettyStatus(status)}
    </span>
  );
}

/* ──────────────────── Error Banner ──────────────────── */

function ErrorBanner({ message }) {
  return (
    <div className="flex items-center gap-3 bg-red-500/10 border border-red-500/20 text-red-400 rounded-xl px-5 py-4">
      <AlertCircle className="w-5 h-5 shrink-0" />
      <p className="text-sm">{message}</p>
    </div>
  );
}

/* ──────────────────── Refund Modal ──────────────────── */

function RefundModal({ payment, onClose, onConfirm }) {
  const [amount, setAmount] = useState('');
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const maxRefund = payment.amount ?? 0;

  const handleRefund = async () => {
    const refundAmt = amount ? parseFloat(amount) : maxRefund;
    if (refundAmt <= 0 || refundAmt > maxRefund) {
      setError(`Amount must be between $0.01 and ${fmtAmount(maxRefund, payment.currency)}`);
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      const payload = {};
      if (amount) payload.amount = refundAmt;
      if (reason.trim()) payload.reason = reason.trim();
      await onConfirm(payload);
    } catch (err) {
      setError(err?.response?.data?.message || 'Refund failed. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-bg-surface rounded-xl shadow-copper-sm w-full max-w-md">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-border-subtle">
          <div className="flex items-center gap-3">
            <div className="flex items-center justify-center w-9 h-9 rounded-xl bg-copper-500 shadow-copper-sm">
              <RotateCcw className="w-5 h-5 text-white" />
            </div>
            <h2 className="text-lg font-bold text-text-primary">Issue Refund</h2>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg hover:bg-bg-hover transition text-text-dim hover:text-text-secondary"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="px-6 py-5 space-y-4">
          {/* Payment summary */}
          <div className="bg-bg-hover rounded-xl p-4 space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-text-secondary">Order #</span>
              <span className="font-medium text-text-primary">{payment.orderNumber || payment.orderId}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-text-secondary">Paid</span>
              <span className="font-semibold text-text-primary">{fmtAmount(payment.amount, payment.currency)}</span>
            </div>
          </div>

          {/* Refund amount */}
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-medium text-text-secondary">
              Refund Amount (leave empty for full refund)
            </label>
            <div className="relative">
              <DollarSign className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim" />
              <input
                type="number"
                step="0.01"
                min="0.01"
                max={maxRefund}
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder={`Max: ${fmtAmount(maxRefund, payment.currency)}`}
                className="w-full rounded-xl border border-border-subtle bg-bg-surface py-2.5 pl-10 pr-3 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
              />
            </div>
          </div>

          {/* Reason */}
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-medium text-text-secondary">Reason (optional)</label>
            <textarea
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              rows={3}
              placeholder="Reason for the refund..."
              className="w-full rounded-xl border border-border-subtle bg-bg-surface px-4 py-3 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition resize-y placeholder:text-text-dim"
            />
          </div>

          {/* Error */}
          {error && (
            <div className="flex items-center gap-2 text-sm text-red-400 bg-red-500/10 rounded-xl px-3 py-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Actions */}
          <div className="flex gap-3 pt-2">
            <button
              onClick={onClose}
              disabled={submitting}
              className="flex-1 px-4 py-2.5 bg-bg-hover text-text-primary text-sm font-medium rounded-xl hover:bg-bg-hover transition disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              onClick={handleRefund}
              disabled={submitting}
              className="flex-1 inline-flex items-center justify-center gap-2 px-4 py-2.5 bg-copper-500 text-white text-sm font-semibold rounded-xl hover:bg-copper-600 transition shadow-copper-sm disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {submitting ? (
                <RefreshCw className="w-4 h-4 animate-spin" />
              ) : (
                <RotateCcw className="w-4 h-4" />
              )}
              Refund
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

/* ──────────────────── Payment Detail Modal ──────────────────── */

function PaymentDetailModal({ payment, onClose, onRefund }) {
  if (!payment) return null;

  const canRefund =
    payment.status === 'SUCCEEDED' ||
    payment.status === 'PARTIALLY_REFUNDED';

  const infoItems = [
    { label: 'Payment ID', value: payment.id },
    { label: 'Order #', value: payment.orderNumber || payment.orderId },
    { label: 'Stripe PI', value: payment.stripePaymentIntentId },
    { label: 'Amount', value: fmtAmount(payment.amount, payment.currency) },
    { label: 'Currency', value: (payment.currency || 'USD').toUpperCase() },
    { label: 'Method', value: payment.paymentMethodType || '--' },
    { label: 'Card', value: payment.cardLast4 ? `**** ${payment.cardLast4}` : '--' },
    { label: 'Brand', value: prettyBrand(payment.cardBrand) || '--' },
    { label: 'Status', value: payment.status, badge: true },
    { label: 'Created', value: formatDate(payment.createdAt) },
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose} />

      <div className="relative bg-bg-surface rounded-xl shadow-copper-sm w-full max-w-lg max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="sticky top-0 bg-bg-surface z-10 flex items-center justify-between px-6 py-4 border-b border-border-subtle rounded-t-xl">
          <div className="flex items-center gap-3">
            <div className="flex items-center justify-center w-9 h-9 rounded-xl bg-copper-500 shadow-copper-sm">
              <CreditCard className="w-5 h-5 text-white" />
            </div>
            <h2 className="text-lg font-bold text-text-primary">Payment Detail</h2>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg hover:bg-bg-hover transition text-text-dim hover:text-text-secondary"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="px-6 py-5 space-y-5">
          {/* Info grid */}
          <div className="grid grid-cols-2 gap-x-6 gap-y-4">
            {infoItems.map((item) => (
              <div key={item.label}>
                <p className="text-xs font-medium text-text-dim uppercase tracking-wide">
                  {item.label}
                </p>
                {item.badge ? (
                  <div className="mt-1">
                    <StatusBadge status={item.value} />
                  </div>
                ) : (
                  <p className="text-sm font-semibold text-text-primary mt-0.5 break-all">
                    {item.value}
                  </p>
                )}
              </div>
            ))}
          </div>

          {/* Refunds list */}
          {payment.refunds && payment.refunds.length > 0 && (
            <div>
              <h3 className="text-sm font-semibold text-text-primary mb-2">Refunds</h3>
              <div className="space-y-2">
                {payment.refunds.map((r, idx) => (
                  <div key={r.id || idx} className="bg-copper-500/10 rounded-xl px-4 py-3 flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-copper-500">
                        {fmtAmount(r.amount, payment.currency)}
                      </p>
                      {r.reason && (
                        <p className="text-xs text-copper-500 mt-0.5">{r.reason}</p>
                      )}
                    </div>
                    <div className="text-right">
                      <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-copper-500/10 text-copper-500">
                        {prettyStatus(r.status || 'PENDING')}
                      </span>
                      {r.createdAt && (
                        <p className="text-xs text-text-dim mt-1">{formatDate(r.createdAt)}</p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Actions */}
          <div className="flex items-center gap-3">
            {canRefund && (
              <button
                onClick={() => onRefund(payment)}
                className="inline-flex items-center gap-2 px-4 py-2.5 bg-copper-500 text-white text-sm font-semibold rounded-xl hover:bg-copper-600 transition shadow-copper-sm"
              >
                <RotateCcw className="w-4 h-4" />
                Issue Refund
              </button>
            )}
            {payment.receiptUrl && (
              <a
                href={payment.receiptUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 px-4 py-2.5 bg-copper-500 text-white text-sm font-semibold rounded-xl hover:bg-copper-600 transition shadow-copper-sm"
              >
                <Receipt className="w-4 h-4" />
                View Receipt
                <ExternalLink className="w-3.5 h-3.5" />
              </a>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

/* ──────────────────── Empty State ──────────────────── */

function EmptyState({ hasFilters }) {
  return (
    <div className="bg-bg-surface rounded-xl shadow-copper-sm p-12 flex flex-col items-center justify-center text-text-dim">
      <CreditCard className="w-14 h-14 mb-4 text-text-dim" />
      <p className="text-base font-medium text-text-secondary">No payments found</p>
      <p className="text-sm mt-1">
        {hasFilters
          ? 'Try adjusting your filters to see more results.'
          : 'Payments will appear here once orders are placed.'}
      </p>
    </div>
  );
}

/* ──────────────────── Main Page ──────────────────── */

export default function AdminPayments() {
  // Filters
  const [statusFilter, setStatusFilter] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');

  // Data
  const [payments, setPayments] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Detail modal
  const [selectedPayment, setSelectedPayment] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);

  // Refund modal
  const [refundTarget, setRefundTarget] = useState(null);

  // Refresh key to trigger re-fetch after mutations
  const [refreshKey, setRefreshKey] = useState(0);

  /* ── Fetch payments ── */

  const fetchPayments = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { page, size: PAGE_SIZE };
      if (statusFilter) params.status = statusFilter;
      if (fromDate) params.from = fromDate;
      if (toDate) params.to = toDate;
      const res = await adminPaymentApi.list(params);
      const pageData = extractPage(res);
      setPayments(pageData.items);
      setTotalPages(pageData.totalPages);
      setTotalElements(pageData.total);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load payments.');
      setPayments([]);
    } finally {
      setLoading(false);
    }
  }, [page, statusFilter, fromDate, toDate, refreshKey]);

  useEffect(() => {
    fetchPayments();
  }, [fetchPayments]);

  /* ── Open detail ── */

  const openDetail = async (payment) => {
    setSelectedPayment(payment);
    setDetailLoading(true);
    try {
      const res = await adminPaymentApi.get(payment.id);
      setSelectedPayment(res.data);
    } catch {
      // fallback to list data
    } finally {
      setDetailLoading(false);
    }
  };

  /* ── Refund ── */

  const handleRefund = async (payload) => {
    await adminPaymentApi.refund(refundTarget.id, payload);
    setRefundTarget(null);
    setSelectedPayment(null);
    setRefreshKey((k) => k + 1);
  };

  /* ── Apply / clear filters ── */

  const applyFilters = () => {
    setPage(0);
    fetchPayments();
  };

  const clearFilters = () => {
    setStatusFilter('');
    setFromDate('');
    setToDate('');
    setPage(0);
  };

  const hasFilters = statusFilter || fromDate || toDate;

  /* ── Render ── */

  return (
    <div className="min-h-screen bg-bg-base animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        {/* ── Header ── */}
        <div className="flex items-center gap-3">
          <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-copper-500 shadow-copper-sm">
            <CreditCard className="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-text-primary">Payments</h1>
            <p className="text-sm text-text-secondary">
              Track and manage all payment transactions
            </p>
          </div>
        </div>

        {/* ── Filters ── */}
        <div className="bg-bg-surface rounded-xl shadow-copper-sm p-5">
          <div className="flex items-center gap-2 mb-4">
            <Filter className="w-4 h-4 text-text-dim" />
            <span className="text-sm font-semibold text-text-primary">Filters</span>
          </div>
          <div className="flex flex-wrap items-end gap-4">
            {/* Status */}
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-medium text-text-secondary">Status</label>
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition min-w-[160px]"
              >
                <option value="">All Statuses</option>
                {STATUSES.map((s) => (
                  <option key={s} value={s}>
                    {prettyStatus(s)}
                  </option>
                ))}
              </select>
            </div>

            {/* From date */}
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-medium text-text-secondary">From</label>
              <input
                type="date"
                value={fromDate}
                onChange={(e) => setFromDate(e.target.value)}
                className="rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
              />
            </div>

            {/* To date */}
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-medium text-text-secondary">To</label>
              <input
                type="date"
                value={toDate}
                onChange={(e) => setToDate(e.target.value)}
                className="rounded-xl border border-border-subtle bg-bg-surface px-3 py-2.5 text-sm text-text-primary shadow-copper-sm focus:border-copper-500 focus:ring-1 focus:ring-copper-500/40 outline-none transition"
              />
            </div>

            {/* Buttons */}
            <div className="flex gap-2">
              <button
                onClick={applyFilters}
                className="px-5 py-2.5 bg-copper-500 text-white text-sm font-semibold rounded-xl hover:bg-copper-600 transition shadow-copper-sm"
              >
                Apply
              </button>
              {hasFilters && (
                <button
                  onClick={clearFilters}
                  className="px-4 py-2.5 bg-bg-hover text-text-secondary text-sm font-medium rounded-xl hover:bg-bg-hover transition"
                >
                  Clear
                </button>
              )}
            </div>
          </div>
        </div>

        {/* ── Error ── */}
        {error && <ErrorBanner message={error} />}

        {/* ── Table ── */}
        {loading ? (
          <TableSkeleton />
        ) : payments.length === 0 ? (
          <EmptyState hasFilters={!!hasFilters} />
        ) : (
          <>
            {/* Count */}
            <div className="flex items-center justify-between">
              <p className="text-sm text-text-secondary">
                {totalElements} payment{totalElements !== 1 ? 's' : ''} found
              </p>
              <button
                onClick={fetchPayments}
                className="p-2 rounded-lg hover:bg-bg-hover transition text-text-dim hover:text-text-secondary"
                title="Refresh"
              >
                <RefreshCw className="w-4 h-4" />
              </button>
            </div>

            <div className="bg-bg-surface rounded-xl shadow-copper-sm overflow-hidden">
              {/* Desktop table */}
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="bg-bg-hover text-text-secondary text-xs uppercase tracking-wide border-b border-border-subtle">
                      <th className="text-left px-6 py-3.5 font-medium">Order #</th>
                      <th className="text-left px-4 py-3.5 font-medium">Amount</th>
                      <th className="text-left px-4 py-3.5 font-medium">Method</th>
                      <th className="text-left px-4 py-3.5 font-medium">Card</th>
                      <th className="text-left px-4 py-3.5 font-medium">Status</th>
                      <th className="text-left px-4 py-3.5 font-medium">Date</th>
                      <th className="text-right px-6 py-3.5 font-medium">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border-subtle">
                    {payments.map((p) => (
                      <tr
                        key={p.id}
                        onClick={() => openDetail(p)}
                        className="hover:bg-copper-500/5 cursor-pointer transition-colors"
                      >
                        <td className="px-6 py-4 font-medium text-text-primary">
                          {p.orderNumber || p.orderId || '--'}
                        </td>
                        <td className="px-4 py-4 text-text-primary">
                          {fmtAmount(p.amount, p.currency)}
                        </td>
                        <td className="px-4 py-4 text-text-secondary capitalize">
                          {p.paymentMethodType
                            ? p.paymentMethodType.replace(/_/g, ' ').toLowerCase()
                            : '--'}
                        </td>
                        <td className="px-4 py-4 text-text-secondary">
                          {p.cardLast4 ? (
                            <span className="inline-flex items-center gap-1.5">
                              <CreditCard className="w-3.5 h-3.5 text-text-dim" />
                              <span>**** {p.cardLast4}</span>
                              {p.cardBrand && (
                                <span className="text-text-dim text-xs">
                                  {prettyBrand(p.cardBrand)}
                                </span>
                              )}
                            </span>
                          ) : (
                            '--'
                          )}
                        </td>
                        <td className="px-4 py-4">
                          <StatusBadge status={p.status} />
                        </td>
                        <td className="px-4 py-4 text-text-secondary whitespace-nowrap">
                          {formatDate(p.createdAt)}
                        </td>
                        <td className="px-6 py-4 text-right">
                          <div className="inline-flex items-center gap-1">
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                openDetail(p);
                              }}
                              className="p-1.5 rounded-lg hover:bg-copper-500/10 transition text-text-dim hover:text-copper-500"
                              title="View detail"
                            >
                              <Eye className="w-4 h-4" />
                            </button>
                            {(p.status === 'SUCCEEDED' || p.status === 'PARTIALLY_REFUNDED') && (
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  setRefundTarget(p);
                                }}
                                className="p-1.5 rounded-lg hover:bg-red-500/10 transition text-text-dim hover:text-red-400"
                                title="Issue refund"
                              >
                                <RotateCcw className="w-4 h-4" />
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* ── Pagination ── */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between pt-2">
                <p className="text-sm text-text-secondary">
                  Page {page + 1} of {totalPages}
                </p>
                <div className="flex gap-2">
                  <button
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    disabled={page === 0}
                    className="inline-flex items-center gap-1 px-3 py-2 text-sm font-medium rounded-xl border border-border-subtle bg-bg-surface text-text-primary hover:bg-bg-hover transition disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <ChevronLeft className="w-4 h-4" />
                    Prev
                  </button>
                  <button
                    onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                    disabled={page >= totalPages - 1}
                    className="inline-flex items-center gap-1 px-3 py-2 text-sm font-medium rounded-xl border border-border-subtle bg-bg-surface text-text-primary hover:bg-bg-hover transition disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Next
                    <ChevronRight className="w-4 h-4" />
                  </button>
                </div>
              </div>
            )}
          </>
        )}

        {/* ── Detail Modal ── */}
        {selectedPayment && (
          <PaymentDetailModal
            payment={selectedPayment}
            onClose={() => setSelectedPayment(null)}
            onRefund={(payment) => {
              setSelectedPayment(null);
              setRefundTarget(payment);
            }}
          />
        )}

        {/* ── Refund Modal ── */}
        {refundTarget && (
          <RefundModal
            payment={refundTarget}
            onClose={() => setRefundTarget(null)}
            onConfirm={handleRefund}
          />
        )}
      </div>
    </div>
  );
}
