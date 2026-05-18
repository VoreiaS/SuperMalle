import { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { ChefHat, ShoppingCart, User, LogOut, Menu, X, Package } from 'lucide-react';
import useAuthStore from '../../store/authStore';
import useCartStore from '../../store/cartStore';

export default function Navbar() {
  const { user, logout } = useAuthStore();
  const { itemCount } = useCartStore();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [badgeBounce, setBadgeBounce] = useState(false);

  useEffect(() => {
    if (itemCount > 0) {
      setBadgeBounce(true);
      const t = setTimeout(() => setBadgeBounce(false), 400);
      return () => clearTimeout(t);
    }
  }, [itemCount]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;

  return (
    <nav className="sticky top-0 z-50 bg-bg-base/80 backdrop-blur-xl border-b border-white/[0.06]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-3 group copper-press">
            <div className="w-9 h-9 rounded-lg flex items-center justify-center"
                 style={{ background: 'linear-gradient(135deg, #c8663e, #a84d28)' }}>
              <ChefHat className="w-5 h-5 text-white" />
            </div>
            <span className="font-body font-bold text-lg text-text-primary tracking-tight">
              SuperMalle
            </span>
          </Link>

          {/* Desktop nav */}
          <div className="hidden md:flex items-center gap-8">
            {[
              { to: '/', label: 'Home' },
              { to: '/menu', label: 'Menu' },
              ...(user ? [{ to: '/orders', label: 'My Orders' }] : []),
            ].map((link) => (
              <Link
                key={link.to}
                to={link.to}
                className={`copper-underline font-body font-medium text-sm tracking-wider uppercase py-1 transition-colors ${
                  isActive(link.to) ? 'text-copper-500 active' : 'text-text-secondary hover:text-text-primary'
                }`}
              >
                {link.label}
              </Link>
            ))}
          </div>

          {/* Right side */}
          <div className="flex items-center gap-3">
            {user ? (
              <>
                <Link
                  to="/cart"
                  className="relative p-2 rounded-lg transition-all duration-200 hover:bg-bg-hover copper-press"
                >
                  <ShoppingCart className="w-5 h-5 text-text-secondary" />
                  {itemCount > 0 && (
                    <span
                      className={`absolute -top-0.5 -right-0.5 w-5 h-5 flex items-center justify-center
                        rounded-full text-white text-[11px] font-bold font-body
                        ${badgeBounce ? 'animate-badge-bounce' : ''}`}
                      style={{ background: 'linear-gradient(135deg, #c8663e, #a84d28)' }}
                    >
                      {itemCount}
                    </span>
                  )}
                </Link>

                <Link
                  to="/profile"
                  className="p-2 rounded-lg transition-all duration-200 hover:bg-bg-hover copper-press"
                >
                  <User className="w-5 h-5 text-text-secondary" />
                </Link>

                {user.role === 'ADMIN' && (
                  <Link
                    to="/admin"
                    className="p-2 rounded-lg transition-all duration-200 hover:bg-bg-hover copper-press"
                  >
                    <Package className="w-5 h-5 text-text-secondary" />
                  </Link>
                )}

                <button
                  onClick={handleLogout}
                  className="p-2 rounded-lg transition-all duration-200 hover:bg-bg-hover copper-press"
                  title="Logout"
                >
                  <LogOut className="w-5 h-5 text-text-dim hover:text-copper-500 transition-colors" />
                </button>
              </>
            ) : (
              <div className="flex items-center gap-3">
                <Link
                  to="/login"
                  className="font-body font-medium text-sm text-text-secondary hover:text-text-primary transition-colors"
                >
                  Sign in
                </Link>
                <Link
                  to="/register"
                  className="btn-copper !px-5 !py-2 !text-xs !font-bold uppercase tracking-wider"
                >
                  Sign up
                </Link>
              </div>
            )}

            {/* Mobile menu button */}
            <button
              onClick={() => setMobileOpen(!mobileOpen)}
              className="md:hidden p-2 rounded-lg hover:bg-bg-hover transition-colors copper-press"
            >
              {mobileOpen ? <X className="w-5 h-5 text-text-primary" /> : <Menu className="w-5 h-5 text-text-primary" />}
            </button>
          </div>
        </div>

        {/* Mobile menu */}
        {mobileOpen && (
          <div className="md:hidden border-t border-white/[0.06] py-4 space-y-1 animate-slide-down">
            {[
              { to: '/', label: 'Home' },
              { to: '/menu', label: 'Menu' },
              ...(user ? [{ to: '/orders', label: 'My Orders' }] : []),
              ...(user?.role === 'ADMIN' ? [{ to: '/admin', label: 'Admin' }] : []),
            ].map((link) => (
              <Link
                key={link.to}
                to={link.to}
                onClick={() => setMobileOpen(false)}
                className={`block px-4 py-3 rounded-lg font-body font-medium text-sm transition-colors ${
                  isActive(link.to)
                    ? 'text-copper-500 bg-copper-500/10'
                    : 'text-text-secondary hover:text-text-primary hover:bg-bg-hover'
                }`}
              >
                {link.label}
              </Link>
            ))}
          </div>
        )}
      </div>
    </nav>
  );
}
