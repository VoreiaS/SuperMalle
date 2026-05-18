package com.example.superMalle.controller.admin;

import com.example.superMalle.service.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationService notificationService;

    @PostMapping("/announce")
    public ResponseEntity<Void> broadcastAnnouncement(@Valid @RequestBody AnnouncementRequest request) {
        notificationService.broadcastAnnouncement(request.getMessage());
        return ResponseEntity.ok().build();
    }

    @Data
    public static class AnnouncementRequest {
        @NotBlank(message = "Message is required")
        @Size(max = 1000, message = "Message must not exceed 1000 characters")
        private String message;
    }
}
