package com.example.superMalle.task;

import com.example.superMalle.entity.Order;
import com.example.superMalle.entity.OrderStatusLog;
import com.example.superMalle.entity.enums.OrderStatus;
import com.example.superMalle.repository.OrderRepository;
import com.example.superMalle.repository.OrderStatusLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCancellationTask {

    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;

    @Value("${app.order.auto-cancel-minutes:30}")
    private int autoCancelMinutes;

    @Scheduled(fixedRateString = "${app.order.auto-cancel-interval-ms:300000}")
    @Transactional
    public void autoCancelUnpaidOrders() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(autoCancelMinutes);
            List<Order> unpaidOrders = orderRepository.findUnpaidPendingOrdersOlderThan(cutoff);

            if (unpaidOrders.isEmpty()) return;

            log.info("Auto-cancelling {} unpaid pending orders older than {} minutes",
                    unpaidOrders.size(), autoCancelMinutes);

            for (Order order : unpaidOrders) {
                order.setStatus(OrderStatus.CANCELLED);
                order.setCompletedAt(null);
                order.setCancellationReason("Auto-cancelled: unpaid after " + autoCancelMinutes + " minutes");

                OrderStatusLog statusLog = OrderStatusLog.builder()
                        .order(order)
                        .status(OrderStatus.CANCELLED)
                        .changedBy("system")
                        .note("Auto-cancelled: payment not completed within " + autoCancelMinutes + " minutes")
                        .build();

                orderRepository.save(order);
                orderStatusLogRepository.save(statusLog);
            }

            log.info("Successfully auto-cancelled {} unpaid orders", unpaidOrders.size());
        } catch (Exception e) {
            log.error("Failed to auto-cancel unpaid orders: {}", e.getMessage(), e);
        }
    }
}
