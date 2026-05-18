import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  Truck, ShoppingBag, CreditCard, Banknote, Tag, Receipt,
  ArrowLeft, Loader2, MapPin, MessageSquare, ChevronRight,
  CheckCircle2, Lock,
} from 'lucide-react';
import { orderApi, couponApi } from '../../api/endpoints';
import useCartStore from '../../store/cartStore';
import { formatPrice } from '../../api/helpers';

function Section({ icon: Icon, title, children, className = '' }) {
  return (
    <div className={`card-copper p-6 ${className}`}>
      <h2 className="text-sm font-semibold text-text-secondary uppercase tracking-wide mb-4 flex items-center gap-2 font-body">
        <Icon className="w-4 h-4 text-copper-500" />
        {title}
      </h2>
      {children}
    </div>
  );
}

function ToggleGroup({ options, value, onChange }) {
  return (
    <div className="grid grid-cols-2 gap-3">
      {options.map((opt) => (
        <button
          key={opt.value}
          onClick={() => onChange(opt.value)}
          className={`flex items-center justify-center gap-2 px-4 py-3.5 rounded-lg font-semibold text-sm transition-all ${
            value === opt.value
              ? 'bg-copper-500 text-white shadow-copper-sm'
              : 'bg-bg-hover text-text-secondary hover:bg-copper-500/10 hover:text-copper-500'
          }`}
        >
          <opt.icon className="w-4 h-4" />
          {opt.label}
        </button>
      ))}
    </div>
  );
}

export default function CheckoutPage() {
  const navigate = useNavigate();
  const { cart, fetchCart, clearCart } = useCartStore();

  const [orderType, setOrderType] = useState('DELIVERY');
  const [deliveryAddress, setDeliveryAddress] = useState('');
  const [specialInstructions, setSpecialInstructions] = useState('');
  const [couponCode, setCouponCode] = useState('');
  const [couponApplied, setCouponApplied] = useState(false);
  const [couponValidating, setCouponValidating] = useState(false);
  const [couponError, setCouponError] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState('card');
  const [placing, setPlacing] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  const items = cart?.items ?? [];
  const subtotal = cart?.subtotal ?? 0;
  const tax = cart?.tax ?? 0;
  const total = cart?.total ?? 0;
  const deliveryCharge = total - subtotal - tax > 0 ? total - subtotal - tax : 0;

  useEffect(() => {
    if (!placing && cart !== null && items.length === 0) {
      navigate('/menu');
    }
  }, [cart, items, placing, navigate]);

  const handleApplyCoupon = async () => {
    const code = couponCode.trim();
    if (!code) return;
    setCouponValidating(true);
    setCouponError(null);
    try {
      const res = await couponApi.validate(code);
      const data = res.data;
      if (data.valid) {
        setCouponApplied(true);
      } else {
        setCouponError(data.message || 'Invalid coupon code');
      }
    } catch {
      setCouponError('Failed to validate coupon. Please try again.');
    } finally {
      setCouponValidating(false);
    }
  };

  const handleRemoveCoupon = () => {
    setCouponCode('');
    setCouponApplied(false);
    setCouponError(null);
  };

  const handlePlaceOrder = async () => {
    if (orderType === 'DELIVERY' && !deliveryAddress.trim()) {
      setError('Please enter a delivery address.');
      return;
    }

    setError(null);
    setPlacing(true);

    try {
      const payload = {
        orderType,
        paymentMethod,
        deliveryAddress: orderType === 'DELIVERY' ? deliveryAddress : '',
        specialInstructions,
        couponCode: couponApplied ? couponCode : '',
      };

      const res = await orderApi.place(payload);
      const orderData = res.data;
      const orderId = orderData?.id || orderData?.orderId;

      if (paymentMethod === 'card') {
        navigate(`/checkout/pay/${orderId}`);
        return;
      } else {
        await clearCart();
        navigate(`/orders/${orderId}`);
      }
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to place order. Please try again.');
    } finally {
      setPlacing(false);
    }
  };

  return (
    <div className="min-h-screen bg-bg-base animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />

      <div className="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="font-display text-2xl sm:text-3xl text-text-primary">Checkout</h1>
          <p className="text-text-secondary mt-1">Review your order and complete your purchase</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-6">
            <Section icon={ShoppingBag} title="Order Type">
              <ToggleGroup
                options={[
                  { value: 'DELIVERY', label: 'Delivery', icon: Truck },
                  { value: 'PICKUP', label: 'Pickup', icon: ShoppingBag },
                ]}
                value={orderType}
                onChange={setOrderType}
              />
            </Section>

            {orderType === 'DELIVERY' && (
              <Section icon={MapPin} title="Delivery Address">
                <textarea
                  value={deliveryAddress}
                  onChange={(e) => setDeliveryAddress(e.target.value)}
                  rows={3}
                  placeholder="Enter your full delivery address..."
                  className="input-copper resize-none"
                />
              </Section>
            )}

            <Section icon={MessageSquare} title="Special Instructions">
              <textarea
                value={specialInstructions}
                onChange={(e) => setSpecialInstructions(e.target.value)}
                rows={3}
                placeholder="Any special requests for your order..."
                className="input-copper resize-none"
              />
            </Section>

            <Section icon={Tag} title="Coupon Code">
              {couponApplied ? (
                <div className="flex items-center justify-between px-4 py-3 bg-emerald-500/10 border border-emerald-500/20 rounded-lg">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-5 h-5 text-emerald-500" />
                    <span className="font-semibold text-emerald-400 text-sm">
                      "{couponCode}" applied
                    </span>
                  </div>
                  <button
                    onClick={handleRemoveCoupon}
                    className="text-xs font-semibold text-red-400 hover:text-red-300 transition-colors"
                  >
                    Remove
                  </button>
                </div>
              ) : (
                <div>
                  <div className="flex gap-3">
                    <input
                      type="text"
                      value={couponCode}
                      onChange={(e) => { setCouponCode(e.target.value); setCouponError(null); }}
                      placeholder="Enter coupon code"
                      className="input-copper"
                    />
                    <button
                      onClick={handleApplyCoupon}
                      disabled={!couponCode.trim() || couponValidating}
                      className="btn-copper !px-6 !py-3 shrink-0"
                    >
                      {couponValidating ? <Loader2 className="w-5 h-5 animate-spin" /> : 'Apply'}
                    </button>
                  </div>
                  {couponError && (
                    <p className="mt-2 text-xs text-red-400">{couponError}</p>
                  )}
                </div>
              )}
            </Section>

            <Section icon={CreditCard} title="Payment Method">
              <ToggleGroup
                options={[
                  { value: 'card', label: 'Card', icon: CreditCard },
                  { value: 'cash', label: 'Cash', icon: Banknote },
                ]}
                value={paymentMethod}
                onChange={setPaymentMethod}
              />
              {paymentMethod === 'card' && (
                <p className="mt-3 text-xs text-text-dim flex items-center gap-1">
                  <Lock className="w-3 h-3" />
                  You will be redirected to confirm your card payment via Stripe.
                </p>
              )}
            </Section>

            {error && (
              <div className="px-4 py-3 bg-red-500/10 border border-red-500/20 rounded-lg text-red-400 text-sm font-medium">
                {error}
              </div>
            )}
          </div>

          <div className="lg:col-span-1">
            <div className="card-copper p-6 sticky top-24">
              <h2 className="text-lg font-bold text-text-primary mb-4 flex items-center gap-2">
                <Receipt className="w-5 h-5 text-copper-500" />
                Order Summary
              </h2>

              <div className="space-y-3 mb-4 max-h-60 overflow-y-auto">
                {items.map((item) => (
                  <div key={item.id} className="flex items-start justify-between text-sm">
                    <div className="flex-1 min-w-0">
                      <p className="font-medium text-text-primary truncate">
                        {item.menuItemName || `Item #${item.menuItemId}`}
                      </p>
                      <p className="text-text-dim text-xs">x{item.quantity}</p>
                    </div>
                    <span className="font-semibold text-text-secondary shrink-0 ml-2">
                      {formatPrice(item.subtotal)}
                    </span>
                  </div>
                ))}
              </div>

              <div className="border-t border-border-subtle pt-4 space-y-2.5 text-sm">
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
                onClick={handlePlaceOrder}
                disabled={placing || items.length === 0}
                className="mt-6 w-full btn-copper !py-4 !text-base !font-bold shadow-copper-md hover:shadow-copper-lg"
              >
                {placing ? (
                  <>
                    <Loader2 className="w-5 h-5 animate-spin" />
                    Placing Order...
                  </>
                ) : (
                  <>
                    Place Order
                    <ChevronRight className="w-5 h-5" />
                  </>
                )}
              </button>

              <Link
                to="/cart"
                className="mt-3 w-full inline-flex items-center justify-center gap-2 px-6 py-3 text-text-secondary font-medium hover:text-copper-500 transition-colors text-sm"
              >
                <ArrowLeft className="w-4 h-4" />
                Back to Cart
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
