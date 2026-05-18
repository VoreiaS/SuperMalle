import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import useAuthStore from '../../store/authStore';
import { useWebSocket } from '../../hooks/useWebSocket';
import client from '../../api/client';
import useToastStore from '../../store/toastStore';

const OrderTrackingPage = () => {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { addToast } = useToastStore();
  const { lastMessage, connectionStatus } = useWebSocket();

  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchOrderDetails();
  }, [orderId]);

  useEffect(() => {
    // Handle WebSocket updates
    if (lastMessage && lastMessage.type === 'ORDER_UPDATE') {
      if (lastMessage.data.orderId === parseInt(orderId)) {
        setOrder(lastMessage.data);
        addToast({
          type: 'info',
          title: 'Order Updated',
          message: `Your order status is now: ${lastMessage.data.status}`
        });
      }
    }
  }, [lastMessage, orderId]);

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

  const getStatusStep = (status) => {
    const steps = {
      PENDING: 1,
      CONFIRMED: 2,
      PREPARING: 3,
      READY: 4,
      OUT_FOR_DELIVERY: 5,
      DELIVERED: 6,
      COMPLETED: 7,
    };
    return steps[status] || 1;
  };

  const formatStatus = (status) => {
    const statusMap = {
      PENDING: { text: 'Pending', description: 'Order received, awaiting confirmation', icon: '📝' },
      CONFIRMED: { text: 'Confirmed', description: 'Order confirmed, preparing to cook', icon: '✅' },
      PREPARING: { text: 'Preparing', description: 'Your order is being prepared', icon: '👨‍🍳' },
      READY: { text: 'Ready', description: 'Order ready for pickup/delivery', icon: '🍔' },
      OUT_FOR_DELIVERY: { text: 'Out for Delivery', description: 'Driver is on the way', icon: '🚚' },
      DELIVERED: { text: 'Delivered', description: 'Order delivered successfully', icon: '🎉' },
      COMPLETED: { text: 'Completed', description: 'Order completed', icon: '✅' },
      CANCELLED: { text: 'Cancelled', description: 'Order has been cancelled', icon: '❌' },
    };
    return statusMap[status] || { text: status, description: '', icon: 'ℹ️' };
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
          <p className="text-text-secondary">Loading order tracking...</p>
        </div>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="min-h-screen bg-bg-base flex items-center justify-center animate-fade-in">
        <div className="text-center">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-500/10 mb-4">
            <svg className="w-8 h-8 text-copper-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-text-primary mb-2">Order Not Found</h2>
          <p className="text-text-secondary mb-6">{error || 'We couldn\'t find your order details'}</p>
          <button
            onClick={() => navigate('/menu')}
            className="inline-flex items-center px-4 py-2 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-copper-500 hover:bg-copper-600"
          >
            Continue Shopping
          </button>
        </div>
      </div>
    );
  }

  const currentStep = getStatusStep(order.status);
  const statusInfo = formatStatus(order.status);

  const steps = [
    { id: 1, name: 'Pending', description: 'Order received' },
    { id: 2, name: 'Confirmed', description: 'Order confirmed' },
    { id: 3, name: 'Preparing', description: 'Being prepared' },
    { id: 4, name: 'Ready', description: 'Ready for delivery/pickup' },
    { id: 5, name: 'Out for Delivery', description: 'On the way' },
    { id: 6, name: 'Delivered', description: 'Delivered' },
    { id: 7, name: 'Completed', description: 'Completed' },
  ];

  return (
    <div className="min-h-screen bg-bg-base py-12 px-4 sm:px-6 lg:px-8 relative animate-fade-in">
      <div className="absolute inset-0 bg-noise pointer-events-none opacity-40" />
      <div className="max-w-3xl mx-auto">
        {/* Header */}
        <div className="bg-bg-surface shadow-copper-md rounded-xl overflow-hidden mb-6">
          <div className="px-6 py-4 border-b border-border-subtle">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-2xl font-bold text-text-primary">Track Your Order</h1>
                <p className="text-sm text-text-secondary mt-1">Order #{order.id}</p>
              </div>
              <div className="flex items-center space-x-2">
                <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${
                  connectionStatus === 'connected' ? 'bg-emerald-500/10 text-emerald-500' : 'bg-bg-hover text-text-primary'
                }`}>
                  <span className="w-2 h-2 rounded-full mr-2 bg-current animate-pulse"></span>
                  {connectionStatus === 'connected' ? 'Live' : 'Offline'}
                </span>
              </div>
            </div>
          </div>

          <div className="p-6">
            {/* Current Status */}
            <div className="text-center mb-8">
              <div className="inline-flex items-center justify-center w-20 h-20 rounded-full bg-copper-500/10 mb-4">
                <span className="text-4xl">{statusInfo.icon}</span>
              </div>
              <h2 className="text-2xl font-bold text-text-primary mb-2">{statusInfo.text}</h2>
              <p className="text-text-secondary">{statusInfo.description}</p>
            </div>

            {/* Progress Steps */}
            <div className="mb-8">
              <div className="relative">
                <div className="absolute top-5 left-0 right-0 h-0.5 bg-border-subtle"></div>
                <div className="relative flex justify-between">
                  {steps.map((step, index) => {
                    const isCompleted = index + 1 < currentStep;
                    const isCurrent = index + 1 === currentStep;
                    const isPending = index + 1 > currentStep;

                    return (
                      <div key={step.id} className="flex flex-col items-center">
                        <div className={`w-10 h-10 rounded-full flex items-center justify-center border-2 ${
                          isCompleted
                            ? 'bg-emerald-500 border-emerald-500 text-white'
                            : isCurrent
                            ? 'bg-copper-500 border-copper-500 text-white'
                            : 'bg-bg-surface border-border-subtle text-text-dim'
                        }`}>
                          {isCompleted ? (
                            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                            </svg>
                          ) : (
                            <span className="text-sm font-medium">{step.id}</span>
                          )}
                        </div>
                        <div className="mt-2 text-center">
                          <p className={`text-sm font-medium ${
                            isCompleted || isCurrent ? 'text-text-primary' : 'text-text-dim'
                          }`}>
                            {step.name}
                          </p>
                          <p className="text-xs text-text-secondary mt-1">{step.description}</p>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>

            {/* Order Timeline */}
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-text-primary mb-4">Order Timeline</h3>
              <div className="relative">
                <div className="absolute left-4 top-0 bottom-0 w-0.5 bg-border-subtle"></div>
                <div className="space-y-6">
                  <div className="relative flex items-start pl-10">
                    <div className="absolute left-2 w-5 h-5 rounded-full bg-emerald-500 border-2 border-white"></div>
                    <div>
                      <p className="text-sm font-medium text-text-primary">Order Placed</p>
                      <p className="text-xs text-text-secondary">
                        {new Date(order.createdAt).toLocaleString()}
                      </p>
                    </div>
                  </div>
                  {order.confirmedAt && (
                    <div className="relative flex items-start pl-10">
                      <div className="absolute left-2 w-5 h-5 rounded-full bg-emerald-500 border-2 border-white"></div>
                      <div>
                        <p className="text-sm font-medium text-text-primary">Order Confirmed</p>
                        <p className="text-xs text-text-secondary">
                          {new Date(order.confirmedAt).toLocaleString()}
                        </p>
                      </div>
                    </div>
                  )}
                  {order.preparingAt && (
                    <div className="relative flex items-start pl-10">
                      <div className="absolute left-2 w-5 h-5 rounded-full bg-emerald-500 border-2 border-white"></div>
                      <div>
                        <p className="text-sm font-medium text-text-primary">Started Preparing</p>
                        <p className="text-xs text-text-secondary">
                          {new Date(order.preparingAt).toLocaleString()}
                        </p>
                      </div>
                    </div>
                  )}
                  {order.readyAt && (
                    <div className="relative flex items-start pl-10">
                      <div className="absolute left-2 w-5 h-5 rounded-full bg-emerald-500 border-2 border-white"></div>
                      <div>
                        <p className="text-sm font-medium text-text-primary">Ready for Delivery</p>
                        <p className="text-xs text-text-secondary">
                          {new Date(order.readyAt).toLocaleString()}
                        </p>
                      </div>
                    </div>
                  )}
                  {order.deliveredAt && (
                    <div className="relative flex items-start pl-10">
                      <div className="absolute left-2 w-5 h-5 rounded-full bg-emerald-500 border-2 border-white"></div>
                      <div>
                        <p className="text-sm font-medium text-text-primary">Delivered</p>
                        <p className="text-xs text-text-secondary">
                          {new Date(order.deliveredAt).toLocaleString()}
                        </p>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Order Summary */}
        <div className="bg-bg-surface shadow-copper-md rounded-xl overflow-hidden mb-6">
          <div className="px-6 py-4 border-b border-border-subtle">
            <h2 className="text-xl font-bold text-text-primary">Order Summary</h2>
          </div>
          <div className="p-6">
            <div className="space-y-3">
              {order.items && order.items.map((item) => (
                <div key={item.id} className="flex items-center justify-between py-2 border-b border-border-subtle">
                  <div className="flex items-center space-x-3">
                    <div className="w-12 h-12 rounded-lg overflow-hidden bg-bg-hover">
                      {item.imageUrl ? (
                        <img
                          src={item.imageUrl}
                          alt={item.name}
                          className="w-full h-full object-cover"
                        />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-text-dim">
                          <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
            <div className="mt-4 pt-4 border-t border-border-subtle">
              <div className="flex justify-between text-lg font-bold">
                <span>Total</span>
                <span className="text-copper-500">${order.total?.toFixed(2) || '0.00'}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row gap-4">
          <button
            onClick={() => navigate('/orders')}
            className="flex-1 flex items-center justify-center px-6 py-3 border border-border-subtle rounded-lg shadow-sm text-sm font-medium text-text-primary bg-bg-surface hover:bg-bg-hover transition-colors"
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 10h16M4 14h16M4 18h16" />
            </svg>
            View All Orders
          </button>
          <button
            onClick={() => navigate('/menu')}
            className="flex-1 flex items-center justify-center px-6 py-3 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-copper-500 hover:bg-copper-600 transition-colors"
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            Order Again
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

export default OrderTrackingPage;
