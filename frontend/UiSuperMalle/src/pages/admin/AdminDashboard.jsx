import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  DollarSign,
  ShoppingCart,
  TrendingUp,
  Users,
  Activity,
  AlertCircle,
  UtensilsCrossed,
  CalendarCheck,
  ArrowRight,
  ClipboardList,
  Tag,
  Settings,
} from 'lucide-react';
import { adminDashboardApi } from '../../api/endpoints';
import { formatPrice, formatDate, getStatusConfig } from '../../api/helpers';

/* ──────────────────── Skeleton helpers ──────────────────── */

function Skeleton({ className = '' }) {
  return <div className={`animate-shimmer rounded-lg bg-bg-hover ${className}`} />;
}

function KpiCardSkeleton() {
  return (
    <div className="bg-bg-surface rounded-xl shadow-copper-sm p-6">
      <div className="flex items-center gap-4">
        <Skeleton className="h-12 w-12 rounded-xl" />
        <div className="flex-1 space-y-2">
          <Skeleton className="h-3 w-24" />
          <Skeleton className="h-6 w-20" />
        </div>
      </div>
    </div>
  );
}

/* ──────────────────── KPI Card ──────────────────── */

function KpiCard({ icon: Icon, label, value, gradient }) {
  return (
    <div className="bg-bg-surface rounded-xl shadow-copper-sm hover:shadow-copper-md transition-shadow p-6">
      <div className="flex items-center gap-4">
        <div
          className={`flex items-center justify-center w-12 h-12 rounded-xl bg-gradient-to-br ${gradient} shadow-sm`}
        >
          <Icon className="w-6 h-6 text-white" />
        </div>
        <div>
          <p className="text-sm font-medium text-text-secondary">{label}</p>
          <p className="text-2xl font-bold text-text-primary mt-0.5">{value}</p>
        </div>
      </div>
    </div>
  );
}

/* ──────────────────── Status Badge (from helpers) ──────────────────── */

function StatusBadge({ status }) {
  const cfg = getStatusConfig(status);
  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold ${cfg.bg} ${cfg.text}`}
    >
      <span className={`w-1.5 h-1.5 rounded-full ${cfg.dot}`} />
      {prettyStatus(status)}
    </span>
  );
}

function prettyStatus(s) {
  if (!s) return '';
  return s.replace(/_/g, ' ');
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

/* ──────────────────── Quick Link Card ──────────────────── */

function QuickLink({ to, icon: Icon, label, description, gradient }) {
  return (
    <Link
      to={to}
      className="bg-bg-surface rounded-xl shadow-copper-sm hover:shadow-copper-md transition-shadow p-5 group"
    >
      <div className="flex items-center gap-4">
        <div
          className={`flex items-center justify-center w-10 h-10 rounded-xl bg-gradient-to-br ${gradient} shadow-sm group-hover:scale-110 transition-transform`}
        >
          <Icon className="w-5 h-5 text-white" />
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-semibold text-text-primary">{label}</p>
          <p className="text-xs text-text-secondary truncate">{description}</p>
        </div>
        <ArrowRight className="w-4 h-4 text-text-dim group-hover:text-copper-500 transition" />
      </div>
    </Link>
  );
}

/* ──────────────────── Main Page ──────────────────── */

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [recentOrders, setRecentOrders] = useState([]);
  const [loadingStats, setLoadingStats] = useState(true);
  const [loadingRecent, setLoadingRecent] = useState(true);
  const [errorStats, setErrorStats] = useState(null);
  const [errorRecent, setErrorRecent] = useState(null);

  useEffect(() => {
    let cancelled = false;

    async function fetchStats() {
      try {
        const res = await adminDashboardApi.stats();
        if (!cancelled) setStats(res.data);
      } catch (err) {
        if (!cancelled) setErrorStats(err?.response?.data?.message || 'Failed to load dashboard stats.');
      } finally {
        if (!cancelled) setLoadingStats(false);
      }
    }

    async function fetchRecentOrders() {
      try {
        const res = await adminDashboardApi.recentOrders();
        if (!cancelled) setRecentOrders(Array.isArray(res.data) ? res.data : []);
      } catch (err) {
        if (!cancelled) setErrorRecent(err?.response?.data?.message || 'Failed to load recent orders.');
      } finally {
        if (!cancelled) setLoadingRecent(false);
      }
    }

    fetchStats();
    fetchRecentOrders();

    return () => {
      cancelled = true;
    };
  }, []);

  /* ── KPI Cards ── */
  const kpiCards = [
    {
      icon: DollarSign,
      label: 'Total Revenue',
      value: formatPrice(stats?.totalRevenue),
      gradient: 'from-copper-500 to-amber-600',
    },
    {
      icon: ShoppingCart,
      label: 'Total Orders',
      value: stats?.totalOrders != null ? stats.totalOrders.toLocaleString() : '--',
      gradient: 'from-blue-500 to-cyan-400',
    },
    {
      icon: TrendingUp,
      label: 'Active Orders',
      value: stats?.activeOrders != null ? stats.activeOrders.toLocaleString() : '--',
      gradient: 'from-copper-500 to-red-400',
    },
    {
      icon: CalendarCheck,
      label: "Today's Orders",
      value: stats?.todayOrders != null ? stats.todayOrders.toLocaleString() : '--',
      gradient: 'from-emerald-500 to-green-400',
    },
    {
      icon: Users,
      label: 'Total Customers',
      value: stats?.totalCustomers != null ? stats.totalCustomers.toLocaleString() : '--',
      gradient: 'from-purple-500 to-violet-400',
    },
    {
      icon: UtensilsCrossed,
      label: 'Menu Items',
      value: stats?.totalMenuItems != null ? stats.totalMenuItems.toLocaleString() : '--',
      gradient: 'from-pink-500 to-rose-400',
    },
  ];

  /* ── Quick Links ── */
  const quickLinks = [
    {
      to: '/admin/orders',
      icon: ClipboardList,
      label: 'Manage Orders',
      description: 'View and update order statuses',
      gradient: 'from-copper-500 to-amber-600',
    },
    {
      to: '/admin/menu',
      icon: UtensilsCrossed,
      label: 'Menu Management',
      description: 'Add, edit or remove menu items',
      gradient: 'from-blue-500 to-cyan-400',
    },
    {
      to: '/admin/coupons',
      icon: Tag,
      label: 'Coupons',
      description: 'Create and manage discount codes',
      gradient: 'from-emerald-500 to-green-400',
    },
    {
      to: '/admin/settings',
      icon: Settings,
      label: 'Settings',
      description: 'Restaurant configuration',
      gradient: 'from-purple-500 to-violet-400',
    },
  ];

  return (
    <div className="min-h-screen bg-bg-base relative animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8 relative">
        {/* ── Header ── */}
        <div>
          <h1 className="text-3xl font-bold text-text-primary">Dashboard</h1>
          <p className="mt-1 text-text-secondary">
            Welcome back! Here's an overview of how your restaurant is doing today.
          </p>
        </div>

        {/* ── Error banners ── */}
        {errorStats && <ErrorBanner message={errorStats} />}
        {errorRecent && <ErrorBanner message={errorRecent} />}

        {/* ── KPI Cards ── */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {loadingStats
            ? Array.from({ length: 6 }).map((_, i) => <KpiCardSkeleton key={i} />)
            : kpiCards.map((card) => (
                <KpiCard
                  key={card.label}
                  icon={card.icon}
                  label={card.label}
                  value={card.value}
                  gradient={card.gradient}
                />
              ))}
        </div>

        {/* ── Recent Orders ── */}
        <div className="bg-bg-surface rounded-xl shadow-copper-sm">
          <div className="flex items-center justify-between px-6 py-4 border-b border-border-subtle">
            <div className="flex items-center gap-3">
              <Activity className="w-5 h-5 text-copper-500" />
              <h2 className="text-lg font-semibold text-text-primary">Recent Orders</h2>
            </div>
            <Link
              to="/admin/orders"
              className="inline-flex items-center gap-1.5 text-sm font-medium text-copper-500 hover:text-copper-500 transition"
            >
              View all
              <ArrowRight className="w-4 h-4" />
            </Link>
          </div>

          {loadingRecent ? (
            <div className="px-6 py-8 space-y-4">
              {Array.from({ length: 5 }).map((_, i) => (
                <div key={i} className="flex items-center gap-4">
                  <Skeleton className="h-4 w-20" />
                  <Skeleton className="h-4 w-24" />
                  <Skeleton className="h-4 w-16" />
                  <Skeleton className="h-6 w-20 rounded-full" />
                  <Skeleton className="h-4 w-28 ml-auto" />
                </div>
              ))}
            </div>
          ) : recentOrders.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-text-dim">
              <ShoppingCart className="w-12 h-12 mb-3 text-text-dim" />
              <p className="text-sm font-medium">Recent orders will appear here</p>
              <p className="text-xs mt-1">Orders from your customers show up in real time</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="bg-bg-hover">
                    <th className="text-left px-6 py-3 text-xs font-semibold text-text-secondary uppercase tracking-wider">
                      Order #
                    </th>
                    <th className="text-left px-4 py-3 text-xs font-semibold text-text-secondary uppercase tracking-wider">
                      Customer
                    </th>
                    <th className="text-right px-4 py-3 text-xs font-semibold text-text-secondary uppercase tracking-wider">
                      Total
                    </th>
                    <th className="text-left px-4 py-3 text-xs font-semibold text-text-secondary uppercase tracking-wider">
                      Status
                    </th>
                    <th className="text-left px-4 py-3 text-xs font-semibold text-text-secondary uppercase tracking-wider">
                      Date
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border-subtle">
                  {recentOrders.map((order) => (
                    <tr
                      key={order.id}
                      className="hover:bg-copper-500/5 transition-colors"
                    >
                      <td className="px-6 py-3.5">
                        <span className="font-mono font-semibold text-text-primary">
                          #{order.orderNumber}
                        </span>
                      </td>
                      <td className="px-4 py-3.5 text-text-primary">
                        {order.customerName || order.userId || '--'}
                      </td>
                      <td className="px-4 py-3.5 text-right font-semibold text-text-primary">
                        {formatPrice(order.finalTotal ?? order.total)}
                      </td>
                      <td className="px-4 py-3.5">
                        <StatusBadge status={order.status} />
                      </td>
                      <td className="px-4 py-3.5 text-text-secondary">
                        {formatDate(order.createdAt)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* ── Quick Links ── */}
        <div>
          <h2 className="text-lg font-semibold text-text-primary mb-4">Quick Actions</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {quickLinks.map((link) => (
              <QuickLink key={link.to} {...link} />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
