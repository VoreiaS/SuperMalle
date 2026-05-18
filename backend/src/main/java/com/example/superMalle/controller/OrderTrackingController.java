package com.example.superMalle.controller;

import com.example.superMalle.entity.Order;
import com.example.superMalle.entity.enums.OrderStatus;
import com.example.superMalle.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket controller for real-time order tracking.
 *
 * Client subscribes to:
 *   /topic/orders/{orderId}/status  -- order status updates
 *   /topic/announcements            -- admin announcements
 *   /user/queue/notifications        -- personal notifications
 *
 * Client sends to:
 *   /app/orders/track               -- start tracking an order
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderTrackingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final OrderRepository orderRepository;

    /**
     * Client sends order ID to start tracking. Server responds with current status.
     * Usage: STOMP SEND to /app/orders/track with payload {"orderId": 123}
     */
    @MessageMapping("/orders/track")
    public void trackOrder(@Payload Map<String, Object> payload, Principal principal) {
        Object orderIdObj = payload.get("orderId");
        if (orderIdObj == null) {
            log.warn("trackOrder called without orderId");
            return;
        }

        Long orderId;
        try {
            orderId = Long.valueOf(orderIdObj.toString());
        } catch (NumberFormatException e) {
            log.warn("Invalid orderId in trackOrder: {}", orderIdObj);
            return;
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            sendError(principal, "Order not found: " + orderId);
            return;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("event", "order:tracking-started");
        response.put("orderId", order.getId());
        response.put("orderNumber", order.getOrderNumber());
        response.put("status", order.getStatus() != null ? order.getStatus().name() : "UNKNOWN");
        response.put("paymentStatus", order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "UNKNOWN");
        response.put("totalAmount", order.getTotalAmount());

        if (principal != null) {
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/notifications", (Object) response);
        }
        // Also publish to the order-specific topic
        messagingTemplate.convertAndSend("/topic/orders/" + orderId + "/status", (Object) response);

        log.info("Order tracking started for order {} by user {}", orderId,
                principal != null ? principal.getName() : "anonymous");
    }

    /**
     * Subscribe mapping: when a client subscribes to /app/orders/subscribe,
     * they immediately get a welcome message.
     */
    @SubscribeMapping("/orders/subscribe")
    public Map<String, Object> onSubscribe(Principal principal) {
        Map<String, Object> welcome = new HashMap<>();
        welcome.put("event", "system:connected");
        welcome.put("message", "Connected to SuperMalle real-time order tracking");
        welcome.put("user", principal != null ? principal.getName() : "anonymous");
        log.info("WebSocket client subscribed: {}", principal != null ? principal.getName() : "anonymous");
        return welcome;
    }

    private void sendError(Principal principal, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("event", "system:error");
        error.put("message", message);
        if (principal != null) {
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors", (Object) error);
        }
    }
}
