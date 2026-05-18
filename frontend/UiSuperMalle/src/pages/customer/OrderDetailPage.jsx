import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  ArrowLeft, MapPin, Package, X, Truck, Clock, AlertCircle, Star, Loader2,
} from 'lucide-react';
import { orderApi } from '../../api/endpoints';
import { formatPrice, getStatusConfig, getPaymentStatusConfig, formatDate } from '../../api/helpers';

const STATUS_STEPS = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'DELIVERED', 'COMPLETED'];

function Skeleton({ className = '' }) {
  return <div className={`animate-shimmer rounded-lg bg-bg-hover ${className}`} />;
}

export default function OrderDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showCancelDialog, setShowCancelDialog] = useState(false);
  const [cancelReason, setCancelReason] = useState('');
  const [cancelling, setCancelling] = useState(false);
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);
  const [reviewSubmitted, setReviewSubmitted] = useState(false);

  useEffect(() => {
    fetchOrder();
  }, [id]);

  const fetchOrder = async () => {
    try {
      const res = await orderApi.getById(id);
      setOrder(res.data);
    } catch {
      setOrder(null);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async () => {
    if (!cancelReason.trim()) return;
    setCancelling(true);
    try {
      await orderApi.cancelOrder(id, { reason: cancelReason.trim() });
      setShowCancelDialog(false);
      setCancelReason('');
      fetchOrder();
    } catch {
      /* keep dialog open */
    } finally {
      setCancelling(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-bg-base pt-20 animate-fade-in">
        <div className="max-w-3xl mx-auto px-4">
          <div className="animate-pulse space-y-6">
            <Skeleton className="h-8 w-1/3" />
            <Skeleton className="h-40 !rounded-xl" />
            <Skeleton className="h-60 !rounded-xl" />
          </div>
        </div>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="min-h-screen bg-bg-base flex items-center justify-center animate-fade-in">
        <div className="text-center">
          <Package className="w-16 h-16 text-text-dim mx-auto mb-4" />
          <h2 className="text-xl font-bold text-text-primary">Order not found</h2>
          <Link to="/orders" className="text-copper-500 mt-2 inline-block hover:text-copper-400">Back to orders</Link>
        </div>
      </div>
    );
  }

  const statusKey = (order.status || '').toUpperCase();
  const currentIdx = STATUS_STEPS.indexOf(statusKey);
  const isCancelled = statusKey === 'CANCELLED';
  const canCancel = ['PENDING', 'CONFIRMED'].includes(statusKey);
  const statusCfg = getStatusConfig(statusKey);
  const payCfg = getPaymentStatusConfig(order.paymentStatus);
  const isDelivery = (order.orderType || '').toUpperCase() === 'DELIVERY';

  return (
    <div className="min-h-screen bg-bg-base animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />

      <div className="relative max-w-3xl mx-auto px-4 py-8">
        <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-text-secondary hover:text-copper-500 mb-6 transition-all">
          <ArrowLeft className="w-4 h-4" /> Back
        </button>

        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="font-display text-2xl text-text-primary">Order #{order.orderNumber}</h1>
            <p className="text-sm text-text-secondary mt-1">{formatDate(order.createdAt)}</p>
          </div>
          <div className="flex items-center gap-3">
            <span className={`px-3 py-1 rounded-full text-xs font-semibold ${statusCfg.bg} ${statusCfg.text}`}>
              {statusKey.replace(/_/g, ' ')}
            </span>
            {canCancel && (
              <button
                onClick={() => setShowCancelDialog(true)}
                className="flex items-center gap-2 px-4 py-2 bg-red-500/10 text-red-400 border border-red-500/20 rounded-lg text-sm font-medium hover:bg-red-500/20 transition-all"
              >
                <X className="w-4 h-4" /> Cancel
              </button>
            )}
          </div>
        </div>

        {showCancelDialog && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm">
            <div className="card-copper p-6 w-full max-w-md mx-4">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-10 h-10 bg-red-500/10 rounded-xl flex items-center justify-center border border-red-500/20">
                  <AlertCircle className="w-5 h-5 text-red-400" />
                </div>
                <h3 className="text-lg font-bold text-text-primary">Cancel Order</h3>
              </div>
              <p className="text-sm text-text-secondary mb-4">
                Please provide a reason for cancelling order #{order.orderNumber}.
              </p>
              <textarea
                value={cancelReason}
                onChange={(e) => setCancelReason(e.target.value)}
                placeholder="Enter cancellation reason..."
                rows={3}
                className="input-copper resize-none"
              />
              <div className="flex gap-3 mt-4">
                <button
                  onClick={() => { setShowCancelDialog(false); setCancelReason(''); }}
                  className="flex-1 btn-ghost !justify-center"
                >
                  Keep Order
                </button>
                <button
                  onClick={handleCancel}
                  disabled={!cancelReason.trim() || cancelling}
                  className="flex-1 px-4 py-2.5 bg-red-600 text-white rounded-lg text-sm font-semibold hover:bg-red-700 disabled:opacity-50 transition-all"
                >
                  {cancelling ? 'Cancelling...' : 'Confirm Cancel'}
                </button>
              </div>
            </div>
          </div>
        )}

        <div className="card-copper p-6 mb-6">
          <h2 className="text-sm font-semibold text-text-dim uppercase tracking-wide mb-4">Order Status</h2>
          {isCancelled ? (
            <div className="flex items-center gap-3 p-4 bg-red-500/10 rounded-lg border border-red-500/20">
              <div className={`w-3 h-3 rounded-full ${statusCfg.dot}`} />
              <span className="font-semibold text-red-400">Order Cancelled</span>
              {order.cancellationReason && (
                <span className="text-sm text-red-400/70 ml-2">— {order.cancellationReason}</span>
              )}
            </div>
          ) : (
            <div className="flex items-center justify-between">
              {STATUS_STEPS.map((step, idx) => {
                const isDone = idx <= currentIdx;
                const isCurrent = idx === currentIdx;
                const label = step.replace(/_/g, ' ');
                return (
                  <div key={step} className="flex flex-col items-center flex-1">
                    <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-all ${
                      isDone ? 'bg-copper-500 text-white shadow-copper-sm' : 'bg-bg-hover text-text-dim'
                    }`}>
                      {isDone ? '\u2713' : idx + 1}
                    </div>
                    <span className={`text-xs mt-2 capitalize ${
                      isCurrent ? 'font-semibold text-copper-500' : isDone ? 'text-text-secondary' : 'text-text-dim'
                    }`}>
                      {label}
                    </span>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        <div className="grid md:grid-cols-2 gap-6 mb-6">
          <div className="card-copper p-5">
            <h3 className="text-sm font-semibold text-text-dim uppercase tracking-wide mb-3">Details</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-text-dim">Type</span>
                <span className="font-medium flex items-center gap-1.5 text-text-primary">
                  {isDelivery ? <Truck className="w-3.5 h-3.5" /> : <Package className="w-3.5 h-3.5" />}
                  {order.orderType}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-text-dim">Payment</span>
                <span className="font-medium text-text-primary capitalize">{order.paymentMethod}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-text-dim">Payment Status</span>
                <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${payCfg.bg} ${payCfg.text}`}>
                  {order.paymentStatus}
                </span>
              </div>
              {isDelivery && order.deliveryAddress && (
                <div className="flex items-start gap-2 pt-1">
                  <MapPin className="w-4 h-4 text-text-dim mt-0.5 shrink-0" />
                  <span className="text-text-secondary">{order.deliveryAddress}</span>
                </div>
              )}
              {order.estimatedDeliveryTime && (
                <div className="flex items-center gap-2 pt-1">
                  <Clock className="w-4 h-4 text-text-dim shrink-0" />
                  <span className="text-text-secondary">Est. delivery: {formatDate(order.estimatedDeliveryTime)}</span>
                </div>
              )}
              {order.specialInstructions && (
                <div className="pt-1 text-text-dim italic">"{order.specialInstructions}"</div>
              )}
              {order.couponCode && (
                <div className="flex justify-between pt-1">
                  <span className="text-text-dim">Coupon</span>
                  <span className="font-medium text-copper-500">{order.couponCode}</span>
                </div>
              )}
            </div>
          </div>

          <div className="card-copper p-5">
            <h3 className="text-sm font-semibold text-text-dim uppercase tracking-wide mb-3">Payment Summary</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-text-dim">Subtotal</span>
                <span className="text-text-primary">{formatPrice(order.subtotal)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-text-dim">Tax</span>
                <span className="text-text-primary">{formatPrice(order.tax)}</span>
              </div>
              {(order.discountAmount > 0) && (
                <div className="flex justify-between">
                  <span className="text-text-dim">Discount</span>
                  <span className="text-emerald-500">-{formatPrice(order.discountAmount)}</span>
                </div>
              )}
              <div className="border-t border-border-subtle pt-2 flex justify-between font-bold text-lg">
                <span className="text-text-primary">Total</span>
                <span className="text-copper-500">{formatPrice(order.finalTotal ?? order.total)}</span>
              </div>
            </div>
          </div>
        </div>

        <div className="card-copper p-5">
          <h3 className="text-sm font-semibold text-text-dim uppercase tracking-wide mb-3">Items</h3>
          <div className="space-y-3">
            {order.items?.map((item, index) => (
              <div key={item.id} className="flex items-center justify-between py-2 border-b border-border-subtle last:border-0 animate-slide-up" style={{ animationDelay: `${index * 60}ms` }}>
                <div>
                  <span className="font-medium text-text-primary">{item.menuItemName || `Item #${item.menuItemId}`}</span>
                  <span className="text-text-dim ml-2">x{item.quantity}</span>
                  <span className="text-text-dim ml-2 text-sm">{formatPrice(item.unitPrice)} each</span>
                  {item.customizations && <p className="text-xs text-text-dim mt-0.5">{item.customizations}</p>}
                  {item.specialInstructions && <p className="text-xs text-copper-500/70 mt-0.5 italic">{item.specialInstructions}</p>}
                </div>
                <span className="font-semibold text-text-primary">{formatPrice(item.subtotal)}</span>
              </div>
            ))}
          </div>
        </div>

        {statusKey === 'COMPLETED' && !reviewSubmitted && (
          <div className="card-copper p-5 mt-6">
            {!showReviewForm ? (
              <div className="text-center">
                <Star className="w-10 h-10 text-copper-500 mx-auto mb-2" />
                <h3 className="font-semibold text-text-primary mb-1">Enjoyed your order?</h3>
                <p className="text-sm text-text-secondary mb-4">Leave a review and help others choose</p>
                <button onClick={() => setShowReviewForm(true)} className="btn-copper">Write a Review</button>
              </div>
            ) : (
              <div>
                <h3 className="font-semibold text-text-primary mb-3">Rate Your Experience</h3>
                <div className="flex items-center gap-1 mb-4">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button key={star} onClick={() => setReviewRating(star)}
                      className="p-1 transition hover:scale-110">
                      <Star className={`w-7 h-7 ${star <= reviewRating ? 'text-copper-500 fill-copper-500' : 'text-border-subtle'}`} />
                    </button>
                  ))}
                </div>
                <textarea value={reviewComment} onChange={(e) => setReviewComment(e.target.value)}
                  placeholder="Share your thoughts about the food..."
                  rows={3} className="input-copper resize-none mb-3" />
                <div className="flex gap-3">
                  <button onClick={async () => {
                    setSubmittingReview(true);
                    try {
                      await orderApi.review(id, { rating: reviewRating, comment: reviewComment });
                      setReviewSubmitted(true);
                      setShowReviewForm(false);
                    } catch { /* ignore */ }
                    finally { setSubmittingReview(false); }
                  }}
                    disabled={submittingReview}
                    className="btn-copper">
                    {submittingReview ? <><Loader2 className="w-4 h-4 animate-spin" /> Submitting...</> : 'Submit Review'}
                  </button>
                  <button onClick={() => setShowReviewForm(false)} className="px-4 py-2 text-text-secondary hover:text-text-primary transition">Cancel</button>
                </div>
              </div>
            )}
          </div>
        )}

        {reviewSubmitted && (
          <div className="card-copper p-5 mt-6 text-center">
            <Star className="w-8 h-8 text-emerald-500 mx-auto mb-2 fill-emerald-500" />
            <p className="font-semibold text-text-primary">Review submitted!</p>
            <p className="text-sm text-text-secondary">Thank you for your feedback.</p>
          </div>
        )}

        {order.statusLog?.length > 0 && (
          <div className="card-copper p-5 mt-6">
            <h3 className="text-sm font-semibold text-text-dim uppercase tracking-wide mb-3">Timeline</h3>
            <div className="space-y-3">
              {order.statusLog.map((log, idx) => {
                const cfg = getStatusConfig(log.status);
                return (
                  <div key={idx} className="flex items-start gap-3">
                    <div className="flex flex-col items-center">
                      <div className={`w-2.5 h-2.5 rounded-full mt-1.5 ${cfg.dot}`} />
                      {idx < order.statusLog.length - 1 && (
                        <div className="w-px h-full bg-border-subtle mt-1" />
                      )}
                    </div>
                    <div>
                      <p className="text-sm font-medium capitalize text-text-primary">{(log.status || '').replace(/_/g, ' ').toLowerCase()}</p>
                      {log.note && <p className="text-xs text-text-dim">{log.note}</p>}
                      <p className="text-xs text-text-dim">{formatDate(log.timestamp)}</p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
