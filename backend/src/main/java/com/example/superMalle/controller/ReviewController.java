package com.example.superMalle.controller;

import com.example.superMalle.dto.order.ReviewRequest;
import com.example.superMalle.dto.order.ReviewResponse;
import com.example.superMalle.dto.menu.PagedResponse;
import com.example.superMalle.entity.Review;
import com.example.superMalle.entity.User;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.exception.ResourceNotFoundException;
import com.example.superMalle.repository.MenuItemRepository;
import com.example.superMalle.repository.OrderRepository;
import com.example.superMalle.repository.ReviewRepository;
import com.example.superMalle.repository.UserRepository;
import com.example.superMalle.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    @GetMapping("/menu-item/{menuItemId}")
    public ResponseEntity<PagedResponse<ReviewResponse>> getReviewsForMenuItem(
            @PathVariable Long menuItemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviewPage = reviewRepository.findByMenuItemId(menuItemId, pageable);
        var responses = reviewPage.getContent().stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsApproved()))
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(PagedResponse.<ReviewResponse>builder()
                .items(responses)
                .total(reviewPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(reviewPage.getTotalPages())
                .build());
    }

    @GetMapping("/my")
    public ResponseEntity<PagedResponse<ReviewResponse>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getAuthenticatedUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviewPage = reviewRepository.findByUserId(userId, pageable);
        return ResponseEntity.ok(PagedResponse.<ReviewResponse>builder()
                .items(reviewPage.getContent().stream().map(this::toResponse).toList())
                .total(reviewPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(reviewPage.getTotalPages())
                .build());
    }

    @PostMapping("/order/{orderId}")
    public ResponseEntity<ReviewResponse> createReview(@PathVariable Long orderId,
                                                        @Valid @RequestBody ReviewRequest request) {
        Long userId = getAuthenticatedUserId();
        var order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (reviewRepository.findByOrderIdAndUserId(orderId, userId).isPresent()) {
            throw new BadRequestException("You have already reviewed this order");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        var menuItem = request.getMenuItemId() != null
                ? menuItemRepository.findById(request.getMenuItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", request.getMenuItemId()))
                : null;

        Review review = Review.builder()
                .user(user)
                .menuItem(menuItem)
                .order(order)
                .rating(request.getRating())
                .comment(request.getComment())
                .isApproved(false)
                .build();
        review = reviewRepository.save(review);

        return ResponseEntity.ok(toResponse(review));
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getName())
                .menuItemId(review.getMenuItem() != null ? review.getMenuItem().getId() : null)
                .orderId(review.getOrder() != null ? review.getOrder().getId() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .imageUrl(review.getImageUrl())
                .isApproved(review.getIsApproved())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private Long getAuthenticatedUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BadRequestException("Not authenticated");
        }
        return userDetails.getId();
    }
}
