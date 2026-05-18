package com.example.superMalle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Notify admin portal of a new order.
     */
    public void notifyNewOrder(Long orderId, String orderNumber, String customerName, Double totalAmount) {
        log.info("Notifying admin of new order: {}", orderNumber);
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "order:new");
        payload.put("orderId", orderId);
        payload.put("orderNumber", orderNumber != null ? orderNumber : "");
        payload.put("customerName", customerName != null ? customerName : "");
        payload.put("totalAmount", totalAmount != null ? totalAmount : 0.0);
        messagingTemplate.convertAndSend("/topic/admin/orders", (Object) payload);
    }

    /**
     * Notify kitchen display of a new order to prepare.
     */
    public void notifyKitchenNewOrder(Long orderId, String orderNumber, java.util.List<Map<String, Object>> items) {
        log.info("Notifying kitchen of new order: {}", orderNumber);
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "kitchen:new-order");
        payload.put("orderId", orderId);
        payload.put("orderNumber", orderNumber != null ? orderNumber : "");
        payload.put("items", items != null ? items : java.util.List.of());
        messagingTemplate.convertAndSend("/topic/kitchen/orders", (Object) payload);
    }

    /**
     * Notify both admin and customer of order status change.
     */
    public void notifyOrderStatusUpdate(Long orderId, String orderNumber, String newStatus, String previousStatus) {
        log.info("Notifying order status update: {} -> {} for order {}", previousStatus, newStatus, orderNumber);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "order:status-update");
        payload.put("orderId", orderId);
        payload.put("orderNumber", orderNumber != null ? orderNumber : "");
        payload.put("newStatus", newStatus != null ? newStatus : "");
        payload.put("previousStatus", previousStatus != null ? previousStatus : "");

        // Notify admin
        messagingTemplate.convertAndSend("/topic/admin/orders/status", (Object) payload);

        // Notify specific customer via user queue
        messagingTemplate.convertAndSend("/topic/orders/" + orderId + "/status", (Object) payload);
    }

    /**
     * Broadcast announcement to all connected clients.
     */
    public void broadcastAnnouncement(String message) {
        if (message == null || message.isBlank()) {
            log.warn("Attempted to broadcast null/blank announcement, ignoring");
            return;
        }
        log.info("Broadcasting announcement: {}", message);
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "admin:announcement");
        payload.put("message", message);
        payload.put("timestamp", java.time.LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/announcements", (Object) payload);
    }

    /**
     * Notify admin of inventory restock.
     */
    public void notifyInventoryRestock(Long inventoryId, String menuItemName, Integer newQuantity) {
        log.info("Notifying inventory restock: {} - {} units", menuItemName, newQuantity);
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "inventory:restock");
        payload.put("inventoryId", inventoryId);
        payload.put("menuItemName", menuItemName != null ? menuItemName : "");
        payload.put("newQuantity", newQuantity != null ? newQuantity : 0);
        payload.put("timestamp", java.time.LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/admin/inventory", (Object) payload);
    }

    /**
     * Notify admin of low stock items.
     */
    public void notifyLowStock(Long inventoryId, String menuItemName, Integer currentQuantity, Integer reorderLevel) {
        log.warn("Notifying low stock: {} - {} units (reorder at {})", menuItemName, currentQuantity, reorderLevel);
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "inventory:low-stock");
        payload.put("inventoryId", inventoryId);
        payload.put("menuItemName", menuItemName != null ? menuItemName : "");
        payload.put("currentQuantity", currentQuantity != null ? currentQuantity : 0);
        payload.put("reorderLevel", reorderLevel != null ? reorderLevel : 0);
        payload.put("timestamp", java.time.LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/admin/inventory/alerts", (Object) payload);
    }

    /**
     * Notify admin of order modification request.
     */
    public void notifyOrderModificationRequest(Long modificationId, String orderNumber, String modificationType) {
        log.info("Notifying order modification request: {} - {}", orderNumber, modificationType);
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "order:modification-request");
        payload.put("modificationId", modificationId);
        payload.put("orderNumber", orderNumber != null ? orderNumber : "");
        payload.put("modificationType", modificationType != null ? modificationType : "");
        payload.put("timestamp", java.time.LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/admin/order-modifications", (Object) payload);
    }

    /**
     * Notify customer of approved order modification.
     */
    public void notifyOrderModificationApproved(Long modificationId, String orderNumber, String modificationType) {
        log.info("Notifying order modification approved: {} - {}", orderNumber, modificationType);
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "order:modification-approved");
        payload.put("modificationId", modificationId);
        payload.put("orderNumber", orderNumber != null ? orderNumber : "");
        payload.put("modificationType", modificationType != null ? modificationType : "");
        payload.put("timestamp", java.time.LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/orders/" + orderNumber + "/modifications", (Object) payload);
    }

    /**
     * Notify customer of rejected order modification.
     */
    public void notifyOrderModificationRejected(Long modificationId, String orderNumber, String reason) {
        log.info("Notifying order modification rejected: {} - {}", orderNumber, reason);
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "order:modification-rejected");
        payload.put("modificationId", modificationId);
        payload.put("orderNumber", orderNumber != null ? orderNumber : "");
        payload.put("reason", reason != null ? reason : "");
        payload.put("timestamp", java.time.LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/orders/" + orderNumber + "/modifications", (Object) payload);
    }

    /**
     * Notify user of loyalty points earned.
     */
    public void notifyLoyaltyPointsEarned(Long userId, Integer points, Integer newBalance) {
        log.info("Notifying loyalty points earned: user {} - {} points (new balance: {})", userId, points, newBalance);
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "loyalty:points-earned");
        payload.put("userId", userId);
        payload.put("points", points != null ? points : 0);
        payload.put("newBalance", newBalance != null ? newBalance : 0);
        payload.put("timestamp", java.time.LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/loyalty", (Object) payload);
    }

    /**
     * Notify user of loyalty points redeemed.
     */
    public void notifyLoyaltyPointsRedeemed(Long userId, Integer points, Integer newBalance) {
        log.info("Notifying loyalty points redeemed: user {} - {} points (new balance: {})", userId, points, newBalance);
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "loyalty:points-redeemed");
        payload.put("userId", userId);
        payload.put("points", points != null ? points : 0);
        payload.put("newBalance", newBalance != null ? newBalance : 0);
        payload.put("timestamp", java.time.LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/loyalty", (Object) payload);
    }

    /**
     * Notify user of tier upgrade.
     */
    public void notifyLoyaltyTierUpgrade(Long userId, String oldTier, String newTier) {
        log.info("Notifying loyalty tier upgrade: user {} - {} -> {}", userId, oldTier, newTier);
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "loyalty:tier-upgrade");
        payload.put("userId", userId);
        payload.put("oldTier", oldTier != null ? oldTier : "");
        payload.put("newTier", newTier != null ? newTier : "");
        payload.put("timestamp", java.time.LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/loyalty", (Object) payload);
    }
}
