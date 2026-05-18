import { useState, useEffect, useCallback } from 'react';
import {
  Star, Search, ThumbsUp, ThumbsDown, Trash2, Loader2,
  RefreshCw, MessageSquare, Filter,
} from 'lucide-react';
import { adminReviewApi } from '../../api/endpoints';

const RATING_COLORS = {
  1: 'text-red-500', 2: 'text-orange-500', 3: 'text-yellow-500',
  4: 'text-lime-500', 5: 'text-emerald-500',
};

export default function AdminReviews() {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [total, setTotal] = useState(0);
  const [pendingOnly, setPendingOnly] = useState(false);
  const [minRating, setMinRating] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [actionLoading, setActionLoading] = useState(null);
  const [actionError, setActionError] = useState(null);

  const fetchReviews = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 20 };
      if (pendingOnly) params.pendingOnly = true;
      if (minRating > 0) params.minRating = minRating;
      const res = await adminReviewApi.list(params);
      setReviews(res.data.items || res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setTotal(res.data.total || 0);
    } catch {
      setReviews([]);
    } finally {
      setLoading(false);
    }
  }, [page, pendingOnly, minRating]);

  useEffect(() => { fetchReviews(); }, [fetchReviews]);

  const handleApprove = async (id) => {
    setActionLoading(id);
    setActionError(null);
    try { await adminReviewApi.approve(id); fetchReviews(); }
    catch { setActionError('Failed to approve review'); }
    finally { setActionLoading(null); }
  };

  const handleReject = async (id) => {
    const notes = prompt('Rejection reason (optional):');
    setActionLoading(id);
    setActionError(null);
    try { await adminReviewApi.reject(id, notes || ''); fetchReviews(); }
    catch { setActionError('Failed to reject review'); }
    finally { setActionLoading(null); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this review permanently?')) return;
    setActionLoading(id);
    setActionError(null);
    try { await adminReviewApi.delete(id); fetchReviews(); }
    catch { setActionError('Failed to delete review'); }
    finally { setActionLoading(null); }
  };

  return (
    <div className="min-h-screen bg-bg-base animate-fade-in relative">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />
      <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="font-display text-2xl text-text-primary">Reviews</h1>
          <p className="text-text-secondary mt-1">{total} review{total !== 1 ? 's' : ''}</p>
        </div>
        <button onClick={fetchReviews} className="btn-copper !px-4 !py-2">
          <RefreshCw className="w-4 h-4" />
          Refresh
        </button>
      </div>

      <div className="flex flex-wrap items-center gap-3 mb-6">
        <div className="relative flex-1 max-w-xs">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-dim" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search reviews..."
            className="input-copper pl-10"
          />
        </div>
        <label className="flex items-center gap-2 text-sm text-text-secondary cursor-pointer">
          <input
            type="checkbox"
            checked={pendingOnly}
            onChange={() => { setPendingOnly(!pendingOnly); setPage(0); }}
            className="rounded border-border-subtle text-copper-500 focus:ring-copper-500"
          />
          <Filter className="w-4 h-4" />
          Pending only
        </label>
        <select
          value={minRating}
          onChange={(e) => { setMinRating(Number(e.target.value)); setPage(0); }}
          className="input-copper !w-auto"
        >
          <option value={0}>All ratings</option>
          <option value={5}>5 stars</option>
          <option value={4}>4+ stars</option>
          <option value={3}>3+ stars</option>
          <option value={2}>2+ stars</option>
          <option value={1}>1+ stars</option>
        </select>
      </div>

      {actionError && (
        <div className="mb-4 px-4 py-3 bg-red-500/10 border border-red-500/20 rounded-lg text-red-400 text-sm font-medium">
          {actionError}
        </div>
      )}

      {loading ? (
        <div className="flex items-center justify-center py-20"><Loader2 className="w-8 h-8 animate-spin text-copper-500" /></div>
      ) : reviews.length === 0 ? (
        <div className="text-center py-20 text-text-dim">No reviews found</div>
      ) : (
        <div className="space-y-4">
          {reviews.map((review) => (
            <div key={review.id} className="card-copper p-5">
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-3 mb-2">
                    <div className="flex items-center gap-0.5">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <Star key={star}
                          className={`w-4 h-4 ${star <= review.rating ? RATING_COLORS[review.rating] : 'text-border-subtle'}`}
                          fill={star <= review.rating ? 'currentColor' : 'none'}
                        />
                      ))}
                    </div>
                    <span className="font-semibold text-text-primary text-sm">{review.userName || `User #${review.userId}`}</span>
                    {review.isApproved === true && <span className="text-xs text-emerald-500 font-semibold">Approved</span>}
                    {review.isApproved === false && <span className="text-xs text-red-400 font-semibold">Rejected</span>}
                    {review.isApproved === null && <span className="text-xs text-yellow-500 font-semibold">Pending</span>}
                  </div>
                  <p className="text-text-primary text-sm mb-1">
                    on <span className="text-copper-500 font-medium">{review.menuItemName || `Item #${review.menuItemId}`}</span>
                    {review.orderNumber && <> &middot; Order #{review.orderNumber}</>}
                  </p>
                  {review.comment && (
                    <div className="bg-bg-hover rounded-lg p-3 mt-2">
                      <MessageSquare className="w-3.5 h-3.5 text-text-dim inline mr-1.5" />
                      <span className="text-text-secondary text-sm">{review.comment}</span>
                    </div>
                  )}
                  {review.moderationNotes && (
                    <p className="text-xs text-text-dim mt-2">Moderation: {review.moderationNotes}</p>
                  )}
                  <p className="text-xs text-text-dim mt-2">{new Date(review.createdAt).toLocaleString()}</p>
                </div>

                <div className="flex items-center gap-2 shrink-0">
                  {review.isApproved === null && (
                    <>
                      <button onClick={() => handleApprove(review.id)}
                        disabled={actionLoading === review.id}
                        className="p-2 rounded-lg bg-emerald-500/10 text-emerald-500 hover:bg-emerald-500/20 transition"
                        title="Approve">
                        {actionLoading === review.id ? <Loader2 className="w-4 h-4 animate-spin" /> : <ThumbsUp className="w-4 h-4" />}
                      </button>
                      <button onClick={() => handleReject(review.id)}
                        disabled={actionLoading === review.id}
                        className="p-2 rounded-lg bg-red-500/10 text-red-400 hover:bg-red-500/20 transition"
                        title="Reject">
                        <ThumbsDown className="w-4 h-4" />
                      </button>
                    </>
                  )}
                  <button onClick={() => handleDelete(review.id)}
                    disabled={actionLoading === review.id}
                    className="p-2 rounded-lg bg-red-500/10 text-red-400 hover:bg-red-500/20 transition"
                    title="Delete">
                    {actionLoading === review.id ? <Loader2 className="w-4 h-4 animate-spin" /> : <Trash2 className="w-4 h-4" />}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 mt-6">
          {Array.from({ length: totalPages }, (_, i) => (
            <button key={i} onClick={() => setPage(i)}
              className={`w-9 h-9 rounded-lg text-sm font-medium transition ${
                i === page ? 'bg-copper-500 text-white' : 'bg-bg-hover text-text-secondary hover:bg-copper-500/20'
              }`}>
              {i + 1}
            </button>
          ))}
        </div>
      )}
      </div>
    </div>
  );
}
