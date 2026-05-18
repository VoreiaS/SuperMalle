import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import useAuthStore from '../../store/authStore';
import client from '../../api/client';
import useToastStore from '../../store/toastStore';

const OrderConfirmationPage = () => {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { addToast } = useToastStore();

  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchOrderDetails();
  }, [orderId]);

  const fetchOrderDetails = async () => {
    try {
      setLoading(true);
      const response = await client.get(`/orders/${orderId}`);
      setOrder(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load order details');
      addToast({
        type: 'error',
        title: 'Error',
        message: 'Failed to load order details'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleTrackOrder = () => {
    navigate(`/orders/${orderId}/track`);
  };

  const handleContinueShopping = () => {
    navigate('/menu');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-bg-base flex items-center justify-center animate-fade-in">
        <div className="text-center">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-copper-500/10 mb-4">
            <svg className="animate-spin h-8 w-8 text-copper-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
          </div>
          <p className="text-text-secondary">Loading your order...</p>
        </div>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="min-h-screen bg-bg-base flex items-center justify-center animate-fade-in">
        <div className="text-center">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-100 mb-4">
            <svg className="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-text-primary mb-2">Order Not Found</h2>
          <p className="text-text-secondary mb-6">{error || 'We couldn\'t find your order details'}</p>
          <button
            onClick={() => navigate('/menu')}
            className="inline-flex items-center px-4 py-2 border border-transparent rounded-lg shadow-copper-md text-sm font-medium text-white bg-copper-500 hover:bg-copper-600"
          >
            Continue Shopping
          </button>
        </div>
      </div>
    );
  }

  const formatStatus = (status) => {
    const statusMap = {
      PENDING: { text: 'Pending', color: 'bg-yellow-100 text-yellow-800' },
      CONFIRMED: { text: 'Confirmed', color: 'bg-blue-100 text-blue-800' },
      PREPARING: { text: 'Preparing', color: 'bg-purple-100 text-purple-800' },
      READY: { text: 'Ready', color: 'bg-emerald-500/10 text-emerald-500' },
      OUT_FOR_DELIVERY: { text: 'Out for Delivery', color: 'bg-blue-500/10 text-blue-500' },
      DELIVERED: { text: 'Delivered', color: 'bg-emerald-500/10 text-emerald-500' },
      COMPLETED: { text: 'Completed', color: 'bg-emerald-500/10 text-emerald-500' },
      CANCELLED: { text: 'Cancelled', color: 'bg-red-100 text-red-800' }
    };
    return statusMap[status] || { text: status, color: 'bg-bg-hover text-text-primary' };
  };

  const statusInfo = formatStatus(order.status);

  return (
    <div className="min-h-screen bg-bg-base py-12 px-4 sm:px-6 lg:px-8 relative animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />
      <div className="max-w-3xl mx-auto">
        {/* Success Message */}
        <div className="bg-bg-surface shadow-copper-md rounded-xl overflow-hidden mb-6">
          <div className="px-6 py-8 text-center">
            <div className="inline-flex items-center justify-center w-20 h-20 rounded-full bg-emerald-500/10 mb-4">
              <svg className="w-10 h-10 text-emerald-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h1 className="text-3xl font-bold text-text-primary mb-2">Order Confirmed!</h1>
            <p className="text-text-secondary mb-4">
              Thank you for your order. We've received your order and will begin preparing it shortly.
            </p>
            <div className="inline-flex items-center px-4 py-2 bg-copper-500/10 rounded-lg">
              <span className="text-sm text-text-secondary mr-2">Order Number:</span>
              <span className="text-sm font-bold text-copper-500">#{order.id}</span>
            </div>
          </div>
        </div>

        {/* Order Details */}
        <div className="bg-bg-surface shadow-copper-md rounded-xl overflow-hidden mb-6">
          <div className="px-6 py-4 border-b border-border-subtle">
            <h2 className="text-xl font-bold text-text-primary">Order Details</h2>
          </div>

          <div className="p-6">
            {/* Order Status */}
            <div className="mb-6">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-text-primary">Status</span>
                <span className={`px-3 py-1 rounded-full text-sm font-medium ${statusInfo.color}`}>
                  {statusInfo.text}
                </span>
              </div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-text-primary">Order Date</span>
                <span className="text-sm text-text-secondary">
                  {new Date(order.createdAt).toLocaleString()}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-text-primary">Estimated Time</span>
                <span className="text-sm text-text-secondary">
                  {order.estimatedDeliveryTime
                    ? new Date(order.estimatedDeliveryTime).toLocaleString()
                    : 'Calculating...'}
                </span>
              </div>
            </div>

            {/* Order Items */}
            <div className="mb-6">
              <h3 className="text-lg font-semibold text-text-primary mb-4">Items</h3>
              <div className="space-y-3">
                {order.items && order.items.map((item) => (
                  <div key={item.id} className="flex items-center justify-between py-3 border-b border-border-subtle">
                    <div className="flex items-center space-x-3">
                      <div className="w-16 h-16 rounded-lg overflow-hidden bg-bg-hover">
                        {item.imageUrl ? (
                          <img
                            src={item.imageUrl}
                            alt={item.name}
                            className="w-full h-full object-cover"
                          />
                        ) : (
                          <div className="w-full h-full flex items-center justify-center text-text-dim">
                            <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                            </svg>
                          </div>
                        )}
                      </div>
                      <div>
                        <h4 className="font-medium text-text-primary">{item.name}</h4>
                        <p className="text-sm text-text-secondary">Qty: {item.quantity}</p>
                      </div>
                    </div>
                    <span className="font-semibold text-text-primary">
                      ${(item.price * item.quantity).toFixed(2)}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            {/* Order Summary */}
            <div className="bg-bg-base rounded-lg p-4">
              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-text-secondary">Subtotal</span>
                  <span className="font-medium">${order.subtotal?.toFixed(2) || '0.00'}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-text-secondary">Tax</span>
                  <span className="font-medium">${order.tax?.toFixed(2) || '0.00'}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-text-secondary">Delivery Fee</span>
                  <span className="font-medium">${order.deliveryFee?.toFixed(2) || '0.00'}</span>
                </div>
                <div className="flex justify-between text-lg font-bold pt-2 border-t border-border-subtle">
                  <span>Total</span>
                  <span className="text-copper-500">${order.total?.toFixed(2) || '0.00'}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Delivery Information */}
        {order.deliveryAddress && (
          <div className="bg-bg-surface shadow-copper-md rounded-xl overflow-hidden mb-6">
            <div className="px-6 py-4 border-b border-border-subtle">
              <h2 className="text-xl font-bold text-text-primary">Delivery Information</h2>
            </div>
            <div className="p-6">
              <div className="space-y-3">
                <div>
                  <span className="text-sm font-medium text-text-primary">Address:</span>
                  <p className="text-sm text-text-secondary mt-1">{order.deliveryAddress}</p>
                </div>
                {order.deliveryInstructions && (
                  <div>
                    <span className="text-sm font-medium text-text-primary">Instructions:</span>
                    <p className="text-sm text-text-secondary mt-1">{order.deliveryInstructions}</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row gap-4">
          <button
            onClick={handleTrackOrder}
            className="flex-1 flex items-center justify-center px-6 py-3 border border-transparent rounded-lg shadow-copper-md text-sm font-medium text-white bg-copper-500 hover:bg-copper-600 transition-colors"
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
            </svg>
            Track Order
          </button>
          <button
            onClick={handleContinueShopping}
            className="flex-1 flex items-center justify-center px-6 py-3 border border-border-subtle rounded-lg shadow-copper-md text-sm font-medium text-text-primary bg-bg-surface hover:bg-bg-base transition-colors"
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            Continue Shopping
          </button>
        </div>

        {/* Help Section */}
        <div className="mt-8 text-center">
          <p className="text-sm text-text-secondary mb-2">Need help with your order?</p>
          <button className="text-sm font-medium text-copper-500 hover:text-copper-600">
            Contact Support
          </button>
        </div>
      </div>
    </div>
  );
};

export default OrderConfirmationPage;
