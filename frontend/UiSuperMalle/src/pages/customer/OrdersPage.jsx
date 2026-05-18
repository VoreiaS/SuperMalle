import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { ShoppingBag, Clock, ChevronRight, Package, ChevronLeft, AlertCircle } from 'lucide-react';
import { orderApi } from '../../api/endpoints';
import { formatPrice, getStatusConfig, formatDate } from '../../api/helpers';

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [fetchError, setFetchError] = useState(null);

  useEffect(() => {
    fetchOrders();
  }, [page]);

  const fetchOrders = async () => {
    setLoading(true);
    setFetchError(null);
    try {
      const res = await orderApi.getMyOrders({ page, size: 10, sort: 'createdAt,desc' });
      const data = res.data;
      setOrders(data.items || []);
      setTotalPages(data.totalPages || 1);
      setTotalElements(data.totalElements || 0);
    } catch (err) {
      setOrders([]);
      setFetchError(err.response?.data?.message || 'Failed to load orders');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-bg-base animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />

      <div className="relative max-w-4xl mx-auto px-4 py-8">
        <div className="flex items-center gap-3 mb-8">
          <div className="w-10 h-10 bg-copper-500 rounded-xl flex items-center justify-center shadow-copper-sm">
            <ShoppingBag className="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 className="font-display text-2xl text-text-primary">My Orders</h1>
            <p className="text-sm text-text-secondary">
              {totalElements > 0 ? `${totalElements} order${totalElements !== 1 ? 's' : ''}` : 'Track and manage your orders'}
            </p>
          </div>
        </div>

        {loading ? (
          <div className="space-y-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="animate-shimmer bg-bg-hover rounded-xl p-6">
                <div className="h-5 bg-bg-surface rounded w-1/3 mb-3" />
                <div className="h-4 bg-bg-surface rounded w-2/3" />
              </div>
            ))}
          </div>
        ) : fetchError ? (
          <div className="text-center py-20">
            <AlertCircle className="w-16 h-16 text-red-400 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-text-secondary">Failed to load orders</h3>
            <p className="text-text-dim mt-1">{fetchError}</p>
            <button onClick={fetchOrders} className="btn-copper mt-4 inline-flex">
              Try Again
            </button>
          </div>
        ) : orders.length === 0 ? (
          <div className="text-center py-20">
            <Package className="w-16 h-16 text-text-dim mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-text-secondary">No orders yet</h3>
            <p className="text-text-dim mt-1">Place your first order from our menu</p>
            <Link to="/menu" className="btn-copper mt-4 inline-flex">
              Browse Menu
            </Link>
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order, index) => {
              const statusCfg = getStatusConfig(order.status);
              return (
                <Link
                  key={order.id}
                  to={`/orders/${order.id}`}
                  className="block card-copper p-5 hover:border-copper-500/30 transition-all group animate-slide-up"
                  style={{ animationDelay: `${index * 80}ms` }}
                >
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-3">
                      <span className="text-sm font-mono text-text-dim">#{order.orderNumber}</span>
                      <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${statusCfg.bg} ${statusCfg.text}`}>
                        {(order.status || '').replace(/_/g, ' ')}
                      </span>
                      <span className="text-xs text-text-dim capitalize">{(order.orderType || '').toLowerCase()}</span>
                    </div>
                    <ChevronRight className="w-5 h-5 text-text-dim group-hover:text-copper-500 transition" />
                  </div>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4 text-sm text-text-dim">
                      <span className="flex items-center gap-1">
                        <Clock className="w-4 h-4" />{formatDate(order.createdAt)}
                      </span>
                      <span>{order.items?.length || 0} item{(order.items?.length || 0) !== 1 ? 's' : ''}</span>
                    </div>
                    <span className="text-lg font-bold text-copper-500">{formatPrice(order.finalTotal ?? order.total)}</span>
                  </div>
                </Link>
              );
            })}
          </div>
        )}

        {totalPages > 1 && (
          <div className="flex justify-center gap-2 mt-8">
            <button
              onClick={() => setPage(p => Math.max(0, p - 1))}
              disabled={page === 0}
              className="px-3 py-1.5 rounded-lg border border-border-subtle text-sm text-text-secondary disabled:opacity-40 hover:bg-copper-500/10 hover:text-copper-500 transition-all"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            {Array.from({ length: totalPages }, (_, i) => (
              <button
                key={i}
                onClick={() => setPage(i)}
                className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-all ${
                  page === i
                    ? 'bg-copper-500 text-white shadow-copper-sm'
                    : 'border border-border-subtle text-text-secondary hover:bg-copper-500/10 hover:text-copper-500'
                }`}
              >
                {i + 1}
              </button>
            ))}
            <button
              onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="px-3 py-1.5 rounded-lg border border-border-subtle text-sm text-text-secondary disabled:opacity-40 hover:bg-copper-500/10 hover:text-copper-500 transition-all"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
