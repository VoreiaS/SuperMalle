import React, { useState, useEffect } from 'react';
import { loadStripe } from '@stripe/stripe-js';
import { Elements, CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import { useNavigate, useParams } from 'react-router-dom';
import { orderApi, paymentApi } from '../../api/endpoints';
import { formatPrice } from '../../api/helpers';
import useCartStore from '../../store/cartStore';
import useToastStore from '../../store/toastStore';
import { Loader2, Lock, ArrowLeft, CreditCard } from 'lucide-react';

const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY);

const CheckoutForm = ({ orderId, amount }) => {
  const stripe = useStripe();
  const elements = useElements();
  const navigate = useNavigate();
  const { clearCart } = useCartStore();
  const { addToast } = useToastStore();

  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!stripe || !elements) return;

    setProcessing(true);
    setError(null);

    try {
      const intentRes = await paymentApi.createIntent({
        orderId,
        paymentMethodType: 'card',
      });
      const { clientSecret, paymentIntentId } = intentRes.data;

      const { error: stripeError, paymentIntent } = await stripe.confirmCardPayment(
        clientSecret,
        {
          payment_method: {
            card: elements.getElement(CardElement),
            billing_details: {
              name: 'Customer',
            },
          },
          setup_future_usage: 'off_session',
        }
      );

      if (stripeError) {
        setError(stripeError.message);
        addToast({ type: 'error', title: 'Payment Failed', message: stripeError.message });
      } else if (paymentIntent.status === 'succeeded') {
        addToast({ type: 'success', title: 'Payment Successful', message: 'Your order has been placed!' });
        await clearCart();
        navigate(`/orders/${orderId}/confirmation`);
      }
    } catch (err) {
      const msg = err.response?.data?.message || 'Payment failed. Please try again.';
      setError(msg);
      addToast({ type: 'error', title: 'Payment Error', message: msg });
    } finally {
      setProcessing(false);
    }
  };

  const cardElementOptions = {
    style: {
      base: {
        fontSize: '16px',
        color: '#e2e0dd',
        fontFamily: '"DM Sans", ui-sans-serif, system-ui, sans-serif',
        '::placeholder': { color: '#8a8580' },
      },
      invalid: { color: '#ef4444', iconColor: '#ef4444' },
    },
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div>
        <label className="block text-sm font-semibold text-text-secondary uppercase tracking-wide mb-2">
          Card Details
        </label>
        <div className="p-4 bg-bg-surface rounded-lg border border-border-subtle focus-within:border-copper-500 focus-within:ring-2 focus-within:ring-copper-500/20 transition-all">
          <CardElement options={cardElementOptions} />
        </div>
        <p className="text-xs text-text-dim mt-2">Test mode — use 4242 4242 4242 4242</p>
      </div>

      {error && (
        <div className="px-4 py-3 bg-red-500/10 border border-red-500/20 rounded-lg text-red-400 text-sm font-medium">
          {error}
        </div>
      )}

      <div className="flex items-center justify-between p-4 bg-bg-hover rounded-lg">
        <span className="text-text-secondary text-sm font-medium">Total to Pay</span>
        <span className="text-xl font-extrabold text-copper-500">{formatPrice(amount)}</span>
      </div>

      <button
        type="submit"
        disabled={!stripe || processing}
        className="w-full btn-copper !py-4 !text-base !font-bold shadow-copper-md hover:shadow-copper-lg disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {processing ? (
          <>
            <Loader2 className="w-5 h-5 animate-spin" />
            Processing Payment...
          </>
        ) : (
          <>
            <Lock className="w-5 h-5" />
            Pay {formatPrice(amount)}
          </>
        )}
      </button>
    </form>
  );
};

const StripeCheckoutPage = () => {
  const navigate = useNavigate();
  const { orderId } = useParams();
  const { items } = useCartStore();

  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!orderId) {
      if (items.length === 0) {
        navigate('/menu');
        return;
      }
      setLoading(false);
      return;
    }

    const fetchOrder = async () => {
      try {
        const res = await orderApi.get(orderId);
        setOrder(res.data);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load order');
      } finally {
        setLoading(false);
      }
    };
    fetchOrder();
  }, [orderId, items, navigate]);

  if (loading) {
    return (
      <div className="min-h-screen bg-bg-base flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-copper-500" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-bg-base flex items-center justify-center">
        <div className="text-center">
          <p className="text-red-400 mb-4">{error}</p>
          <button onClick={() => navigate('/checkout')} className="btn-copper">Back to Checkout</button>
        </div>
      </div>
    );
  }

  const totalAmount = order?.totalAmount || items.reduce((sum, i) => sum + (i.subtotal || i.price * i.quantity || 0), 0);

  return (
    <div className="min-h-screen bg-bg-base animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />
      <div className="relative max-w-lg mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="card-copper p-8">
          <div className="w-16 h-16 rounded-xl bg-copper-500/10 flex items-center justify-center mx-auto mb-6 border border-copper-500/20">
            <CreditCard className="w-8 h-8 text-copper-500" />
          </div>

          <h2 className="font-display text-2xl text-text-primary text-center mb-2">Complete Payment</h2>
          <p className="text-text-secondary text-center mb-8">
            Enter your card details to complete order {orderId ? `#${orderId}` : ''}
          </p>

          <Elements stripe={stripePromise}>
            <CheckoutForm orderId={orderId} amount={totalAmount} />
          </Elements>

          <button
            onClick={() => navigate(orderId ? `/orders/${orderId}` : '/checkout')}
            className="mt-4 w-full inline-flex items-center justify-center gap-2 px-6 py-3 text-text-secondary font-medium hover:text-copper-500 transition-colors text-sm"
          >
            <ArrowLeft className="w-4 h-4" />
            {orderId ? 'Back to Order' : 'Back to Checkout'}
          </button>
        </div>

        <div className="mt-6 flex items-center justify-center gap-6 text-sm text-text-dim">
          <span className="flex items-center gap-1.5"><Lock className="w-3.5 h-3.5 text-emerald-500" /> Secure Payment</span>
          <span>SSL Encrypted</span>
          <span>All Cards Accepted</span>
        </div>
      </div>
    </div>
  );
};

export default StripeCheckoutPage;
