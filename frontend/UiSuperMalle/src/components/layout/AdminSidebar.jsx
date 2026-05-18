import { NavLink, useLocation } from 'react-router-dom';
import {
  LayoutDashboard, ShoppingBag, UtensilsCrossed, Tags,
  Receipt, CreditCard, Settings, Megaphone, ChevronLeft,
  ChevronRight, ChefHat, Users, Star, Clock
} from 'lucide-react';
import { useState } from 'react';

const navItems = [
  { to: '/admin', icon: LayoutDashboard, label: 'Dashboard', end: true },
  { to: '/admin/orders', icon: ShoppingBag, label: 'Orders' },
  { to: '/admin/menu', icon: UtensilsCrossed, label: 'Menu' },
  { to: '/admin/categories', icon: Tags, label: 'Categories' },
  { to: '/admin/users', icon: Users, label: 'Users' },
  { to: '/admin/coupons', icon: Receipt, label: 'Coupons' },
  { to: '/admin/payments', icon: CreditCard, label: 'Payments' },
  { to: '/admin/reviews', icon: Star, label: 'Reviews' },
  { to: '/admin/hours', icon: Clock, label: 'Hours' },
  { to: '/admin/settings', icon: Settings, label: 'Settings' },
  { to: '/admin/announce', icon: Megaphone, label: 'Announce' },
];

export default function AdminSidebar() {
  const [collapsed, setCollapsed] = useState(false);
  const location = useLocation();

  return (
    <aside
      className={`h-screen sticky top-0 bg-[#0a0a0a] text-white transition-all duration-300 flex flex-col border-r border-copper-500/20 ${
        collapsed ? 'w-16' : 'w-60'
      }`}
    >
      {/* Header */}
      <div className="flex items-center justify-between px-4 h-16 border-b border-white/[0.06]">
        {!collapsed && (
          <div className="flex items-center gap-2">
            <div
              className="w-7 h-7 rounded flex items-center justify-center"
              style={{ background: 'linear-gradient(135deg, #c8663e, #a84d28)' }}
            >
              <ChefHat className="w-4 h-4 text-white" />
            </div>
            <span className="font-body font-bold text-sm">Admin</span>
          </div>
        )}
        <button
          onClick={() => setCollapsed(!collapsed)}
          className="p-1.5 rounded-lg hover:bg-white/10 transition copper-press"
        >
          {collapsed ? (
            <ChevronRight className="w-4 h-4 text-text-secondary" />
          ) : (
            <ChevronLeft className="w-4 h-4 text-text-secondary" />
          )}
        </button>
      </div>

      {/* Nav */}
      <nav className="flex-1 py-4 space-y-1 px-2">
        {navItems.map((item) => {
          const isActive = item.end
            ? location.pathname === item.to
            : location.pathname.startsWith(item.to);
          return (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={`relative flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-body font-medium transition-all duration-200 ${
                isActive
                  ? 'text-copper-500 bg-copper-500/10'
                  : 'text-text-secondary hover:text-text-primary hover:bg-white/[0.04]'
              }`}
            >
              {isActive && (
                <span className="absolute left-0 top-1/2 -translate-y-1/2 w-0.5 h-5 rounded-r-full bg-copper-500" />
              )}
              <item.icon className="w-5 h-5 shrink-0" />
              {!collapsed && <span>{item.label}</span>}
            </NavLink>
          );
        })}
      </nav>
    </aside>
  );
}
