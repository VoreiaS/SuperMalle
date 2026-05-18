import { useState, useEffect, useCallback, useRef } from 'react';
import { WS_BASE } from '../api/endpoints';

const WS_STATUS = {
  CONNECTING: 'connecting',
  CONNECTED: 'connected',
  DISCONNECTED: 'disconnected',
  ERROR: 'error',
};

export function useWebSocket(options = {}) {
  const {
    onMessage,
    onConnect,
    onDisconnect,
    onError,
    reconnectInterval = 3000,
    maxReconnectAttempts = 5,
  } = options;

  const [status, setStatus] = useState(WS_STATUS.DISCONNECTED);
  const [reconnectAttempts, setReconnectAttempts] = useState(0);
  const wsRef = useRef(null);
  const reconnectTimeoutRef = useRef(null);
  const shouldReconnectRef = useRef(true);

  const connect = useCallback(() => {
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      return;
    }

    setStatus(WS_STATUS.CONNECTING);

    try {
      const token = localStorage.getItem('token');
      const wsUrl = `${WS_BASE}?token=${encodeURIComponent(token)}`;

      wsRef.current = new WebSocket(wsUrl);

      wsRef.current.onopen = () => {
        setStatus(WS_STATUS.CONNECTED);
        setReconnectAttempts(0);
        shouldReconnectRef.current = true;

        if (onConnect) {
          onConnect();
        }
      };

      wsRef.current.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);

          if (onMessage) {
            onMessage(data);
          }
        } catch (err) {
          console.error('Failed to parse WebSocket message:', err);
        }
      };

      wsRef.current.onerror = (error) => {
        console.error('WebSocket error:', error);
        setStatus(WS_STATUS.ERROR);

        if (onError) {
          onError(error);
        }
      };

      wsRef.current.onclose = (event) => {
        setStatus(WS_STATUS.DISCONNECTED);

        if (onDisconnect) {
          onDisconnect(event);
        }

        // Attempt to reconnect if not intentionally closed
        if (shouldReconnectRef.current && !event.wasClean) {
          if (reconnectAttempts < maxReconnectAttempts) {
            setReconnectAttempts((prev) => prev + 1);

            reconnectTimeoutRef.current = setTimeout(() => {
              connect();
            }, reconnectInterval);
          } else {
            console.error('Max reconnection attempts reached');
          }
        }
      };
    } catch (err) {
      console.error('Failed to create WebSocket connection:', err);
      setStatus(WS_STATUS.ERROR);

      if (onError) {
        onError(err);
      }
    }
  }, [onConnect, onDisconnect, onError, onMessage, reconnectInterval, maxReconnectAttempts, reconnectAttempts]);

  const disconnect = useCallback(() => {
    shouldReconnectRef.current = false;

    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }

    if (wsRef.current) {
      wsRef.current.close();
      wsRef.current = null;
    }

    setStatus(WS_STATUS.DISCONNECTED);
  }, []);

  const send = useCallback((data) => {
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      try {
        const message = typeof data === 'string' ? data : JSON.stringify(data);
        wsRef.current.send(message);
        return true;
      } catch (err) {
        console.error('Failed to send WebSocket message:', err);
        return false;
      }
    }
    return false;
  }, []);

  // Auto-connect on mount if token exists
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      connect();
    }

    return () => {
      disconnect();
    };
  }, [connect, disconnect]);

  return {
    status,
    isConnected: status === WS_STATUS.CONNECTED,
    connect,
    disconnect,
    send,
    reconnectAttempts,
  };
}

// Hook for order status updates
export function useOrderUpdates(onOrderUpdate) {
  const handleOrderMessage = useCallback(
    (data) => {
      if (data.type === 'ORDER_UPDATE' && data.order) {
        if (onOrderUpdate) {
          onOrderUpdate(data.order);
        }
      }
    },
    [onOrderUpdate]
  );

  const ws = useWebSocket({
    onMessage: handleOrderMessage,
  });

  return ws;
}

// Hook for admin notifications
export function useAdminNotifications(onNotification) {
  const handleNotificationMessage = useCallback(
    (data) => {
      if (data.type === 'NOTIFICATION' && data.notification) {
        if (onNotification) {
          onNotification(data.notification);
        }
      }
    },
    [onNotification]
  );

  const ws = useWebSocket({
    onMessage: handleNotificationMessage,
  });

  return ws;
}

export { WS_STATUS };
