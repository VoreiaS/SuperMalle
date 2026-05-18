package com.example.superMalle.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class RecentOrderResponse {
    private Long id;
    private String orderNumber;
    private String customerName;
    private Long userId;
    private BigDecimal total;
    private String status;
    private LocalDateTime createdAt;
}
