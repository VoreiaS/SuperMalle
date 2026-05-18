import { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  ShoppingCart, Minus, Plus, Trash2, ArrowRight,
  UtensilsCrossed, ShoppingBag, Receipt, Truck, ArrowLeft,
} from 'lucide-react';
import useCartStore from '../../store/cartStore';
import { formatPrice } from '../../api/helpers';

export default function CartPage() {
  const navigate = useNavigate();
  const { cart, fetchCart, updateItem, removeItem, clearCart, itemCount } = useCartStore();

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  const items = cart?.items ?? [];
  const subtotal = cart?.subtotal ?? 0;
  const tax = cart?.tax ?? 0;
  const total = cart?.total ?? 0;
  const deliveryCharge = total - subtotal - tax > 0 ? total - subtotal - tax : 0;

  const handleQuantityChange = async (itemId, currentQty, delta) => {
    const newQty = currentQty + delta;
    if (newQty <= 0) {
      await removeItem(itemId);
    } else {
      await updateItem(itemId, { quantity: newQty });
    }
  };

  const handleClearCart = async () => {
    await clearCart();
  };

  if (!cart || items.length === 0) {
    return (
      <div className="min-h-screen bg-bg-base flex items-center justify-center animate-fade-in">
        <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />
        <div className="relative text-center max-w-md px-4">
          <div className="w-24 h-24 rounded-xl bg-copper-500/10 flex items-center justify-center mx-auto mb-6 border border-copper-500/20">
            <ShoppingCart className="w-12 h-12 text-copper-500/50" />
          </div>
          <h2 className="text-2xl font-bold text-text-primary mb-2">Your cart is empty</h2>
          <p className="text-text-secondary mb-8 max-w-sm mx-auto">
            Looks like you haven't added anything yet. Explore our menu and find something delicious!
          </p>
          <Link to="/menu" className="btn-copper !px-8 !py-3.5 !text-sm !font-bold uppercase tracking-wider">
            <ShoppingBag className="w-5 h-5" />
            Browse Menu
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-bg-base animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />

      <div className="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="font-display text-2xl sm:text-3xl text-text-primary">Your Cart</h1>
            <p className="text-text-secondary mt-1">{itemCount} item{itemCount !== 1 ? 's' : ''}</p>
          </div>
          <button
            onClick={handleClearCart}
            className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-semibold text-red-400 bg-red-500/10 hover:bg-red-500/20 rounded-lg border border-red-500/20 transition-all"
          >
            <Trash2 className="w-4 h-4" />
            Clear All
          </button>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-4">
            {items.map((item, index) => (
              <div
                key={item.id}
                className="card-copper flex items-start gap-4 p-4 sm:p-5 hover:border-copper-500/20 transition-all animate-slide-up"
                style={{ animationDelay: `${index * 80}ms` }}
              >
                <div className="shrink-0 w-16 h-16 rounded-xl bg-gradient-to-br from-copper-500/30 to-amber-600/20 flex items-center justify-center border border-copper-500/10">
                  <UtensilsCrossed className="w-7 h-7 text-copper-500/50" />
                </div>

                <div className="flex-1 min-w-0">
                  <h3 className="font-semibold text-text-primary truncate">
                    {item.menuItemName || `Item #${item.menuItemId}`}
                  </h3>
                  <p className="text-sm text-copper-500 font-semibold mt-0.5">
                    {formatPrice(item.unitPrice)} each
                  </p>

                  {(item.customizations?.length > 0 || item.specialInstructions) && (
                    <div className="mt-2 text-xs text-text-dim space-y-0.5">
                      {item.customizations?.length > 0 && (
                        <p>
                          <span className="font-medium text-text-secondary">Customizations:</span>{' '}
                          {Array.isArray(item.customizations)
                            ? item.customizations.join(', ')
                            : item.customizations}
                        </p>
                      )}
                      {item.specialInstructions && (
                        <p>
                          <span className="font-medium text-text-secondary">Note:</span>{' '}
                          {item.specialInstructions}
                        </p>
                      )}
                    </div>
                  )}

                  <div className="flex items-center justify-between mt-3">
                    <div className="inline-flex items-center gap-1 bg-bg-hover rounded-lg p-0.5">
                      <button
                        onClick={() => handleQuantityChange(item.id, item.quantity, -1)}
                        className="w-8 h-8 flex items-center justify-center rounded-lg bg-bg-surface text-text-secondary hover:text-copper-500 hover:bg-copper-500/10 shadow-sm transition-all active:scale-95"
                      >
                        <Minus className="w-3.5 h-3.5" />
                      </button>
                      <span className="w-8 text-center text-sm font-bold text-text-primary">
                        {item.quantity}
                      </span>
                      <button
                        onClick={() => handleQuantityChange(item.id, item.quantity, 1)}
                        className="w-8 h-8 flex items-center justify-center rounded-lg bg-bg-surface text-text-secondary hover:text-copper-500 hover:bg-copper-500/10 shadow-sm transition-all active:scale-95"
                      >
                        <Plus className="w-3.5 h-3.5" />
                      </button>
                    </div>

                    <span className="text-lg font-bold text-copper-500">
                      {formatPrice(item.subtotal)}
                    </span>
                  </div>
                </div>

                <button
                  onClick={() => removeItem(item.id)}
                  className="shrink-0 w-9 h-9 flex items-center justify-center text-text-dim hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-all"
                  title="Remove item"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>

          <div className="lg:col-span-1">
            <div className="card-copper p-6 sticky top-24">
              <h2 className="text-lg font-bold text-text-primary mb-4 flex items-center gap-2">
                <Receipt className="w-5 h-5 text-copper-500" />
                Order Summary
              </h2>

              <div className="space-y-3 text-sm">
                <div className="flex items-center justify-between">
                  <span className="text-text-dim">Subtotal</span>
                  <span className="font-semibold text-text-primary">{formatPrice(subtotal)}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-text-dim">Tax</span>
                  <span className="font-semibold text-text-primary">{formatPrice(tax)}</span>
                </div>
                {deliveryCharge > 0 && (
                  <div className="flex items-center justify-between">
                    <span className="text-text-dim flex items-center gap-1">
                      <Truck className="w-3.5 h-3.5" />
                      Delivery
                    </span>
                    <span className="font-semibold text-text-primary">{formatPrice(deliveryCharge)}</span>
                  </div>
                )}

                <div className="border-t border-border-subtle pt-3 mt-3">
                  <div className="flex items-center justify-between">
                    <span className="text-base font-bold text-text-primary">Total</span>
                    <span className="text-xl font-extrabold text-copper-500">
                      {formatPrice(total)}
                    </span>
                  </div>
                </div>
              </div>

              <button
                onClick={() => navigate('/checkout')}
                className="mt-6 w-full btn-copper !py-3.5 !text-sm !font-bold uppercase tracking-wider shadow-copper-md hover:shadow-copper-lg"
              >
                Proceed to Checkout
                <ArrowRight className="w-5 h-5" />
              </button>

              <Link
                to="/menu"
                className="mt-3 w-full inline-flex items-center justify-center gap-2 px-6 py-3 text-text-secondary font-medium hover:text-copper-500 transition-colors text-sm"
              >
                <ArrowLeft className="w-4 h-4" />
                Continue Shopping
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
