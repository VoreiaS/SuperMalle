package com.example.superMalle.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long menuItemId;
    private String menuItemName;
    private Long orderId;
    private String orderNumber;
    private Integer rating;
    private String comment;
    private String imageUrl;
    private Boolean isApproved;
    private String moderationNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
