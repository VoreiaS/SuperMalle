package com.example.superMalle.service;

import com.example.superMalle.dto.admin.ReviewResponse;
import com.example.superMalle.dto.menu.PagedResponse;
import com.example.superMalle.entity.Review;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.exception.ResourceNotFoundException;
import com.example.superMalle.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewRepository reviewRepository;

    public PagedResponse<ReviewResponse> getAllReviews(Long menuItemId, Integer minRating,
                                                        Boolean pendingOnly, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviewPage;

        if (Boolean.TRUE.equals(pendingOnly)) {
            reviewPage = reviewRepository.findByIsApprovedFalse(pageable);
        } else if (menuItemId != null) {
            reviewPage = reviewRepository.findByMenuItemId(menuItemId, pageable);
        } else {
            reviewPage = reviewRepository.findAll(pageable);
        }

        var responses = reviewPage.getContent().stream()
                .map(this::toResponse)
                .filter(r -> minRating == null || r.getRating() >= minRating)
                .toList();

        return PagedResponse.<ReviewResponse>builder()
                .items(responses)
                .total(reviewPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(reviewPage.getTotalPages())
                .build();
    }

    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        return toResponse(review);
    }

    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review", "id", id);
        }
        reviewRepository.deleteById(id);
    }

    @Transactional
    public ReviewResponse approveReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        review.setIsApproved(true);
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponse rejectReview(Long id, String notes) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        review.setIsApproved(false);
        review.setModerationNotes(notes);
        return toResponse(reviewRepository.save(review));
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getName())
                .menuItemId(review.getMenuItem() != null ? review.getMenuItem().getId() : null)
                .menuItemName(review.getMenuItem() != null ? review.getMenuItem().getName() : null)
                .orderId(review.getOrder() != null ? review.getOrder().getId() : null)
                .orderNumber(review.getOrder() != null ? review.getOrder().getOrderNumber() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .imageUrl(review.getImageUrl())
                .isApproved(review.getIsApproved())
                .moderationNotes(review.getModerationNotes())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
