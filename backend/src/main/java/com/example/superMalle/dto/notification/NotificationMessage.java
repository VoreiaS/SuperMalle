package com.example.superMalle.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Notification Message DTO
 * 
 * Represents a notification message to be sent asynchronously
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

    private Long userId;
    private String type;
    private String title;
    private String message;
    private Map<String, Object> data;
    private String priority;
    private Long timestamp;
}
