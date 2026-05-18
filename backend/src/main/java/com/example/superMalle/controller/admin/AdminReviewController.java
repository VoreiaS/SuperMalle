package com.example.superMalle.controller.admin;

import com.example.superMalle.dto.admin.ReviewResponse;
import com.example.superMalle.dto.menu.PagedResponse;
import com.example.superMalle.service.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping
    public ResponseEntity<PagedResponse<ReviewResponse>> getAllReviews(
            @RequestParam(required = false) Long menuItemId,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Boolean pendingOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminReviewService.getAllReviews(menuItemId, minRating, pendingOnly, page, size));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(adminReviewService.getReviewById(id));
    }

    @PostMapping("/{id:\\d+}/approve")
    public ResponseEntity<ReviewResponse> approveReview(@PathVariable Long id) {
        return ResponseEntity.ok(adminReviewService.approveReview(id));
    }

    @PostMapping("/{id:\\d+}/reject")
    public ResponseEntity<ReviewResponse> rejectReview(@PathVariable Long id,
                                                        @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(adminReviewService.rejectReview(id, notes));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        adminReviewService.deleteReview(id);
        return ResponseEntity.ok().build();
    }
}
