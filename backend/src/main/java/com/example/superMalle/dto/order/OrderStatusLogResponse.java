package com.example.superMalle.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusLogResponse {
    private Long id;
    private String status;
    private String changedBy;
    private String note;
    private LocalDateTime createdAt;
}
