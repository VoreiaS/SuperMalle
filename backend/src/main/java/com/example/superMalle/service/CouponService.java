package com.example.superMalle.service;

import com.example.superMalle.dto.admin.CouponRequest;
import com.example.superMalle.dto.admin.CouponResponse;
import com.example.superMalle.dto.menu.PagedResponse;
import com.example.superMalle.entity.Coupon;
import com.example.superMalle.entity.enums.DiscountType;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.exception.ResourceNotFoundException;
import com.example.superMalle.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        validateCouponRequest(request, null);

        // Check for duplicate active code
        if (couponRepository.findByCodeAndIsActiveTrue(request.getCode().toUpperCase()).isPresent()) {
            throw new BadRequestException("Coupon code already exists");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .discountType(request.getDiscountType())
                .value(request.getValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .usageCount(0)
                .isActive(true)
                .expiresAt(request.getExpiresAt())
                .build();

        coupon = couponRepository.save(coupon);
        return toResponse(coupon);
    }

    @Transactional
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));

        validateCouponRequest(request, coupon.getDiscountType());

        if (request.getCode() != null && !request.getCode().isBlank()) {
            String newCode = request.getCode().toUpperCase();
            if (!newCode.equals(coupon.getCode())) {
                // Check for duplicate if code is changing
                if (couponRepository.findByCodeAndIsActiveTrue(newCode)
                        .filter(c -> !c.getId().equals(id))
                        .isPresent()) {
                    throw new BadRequestException("Coupon code already exists");
                }
                coupon.setCode(newCode);
            }
        }
        if (request.getDiscountType() != null) {
            coupon.setDiscountType(request.getDiscountType());
        }
        if (request.getValue() != null) {
            coupon.setValue(request.getValue());
        }
        if (request.getMinOrderAmount() != null) {
            coupon.setMinOrderAmount(request.getMinOrderAmount());
        }
        if (request.getMaxDiscountAmount() != null) {
            coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        }
        if (request.getUsageLimit() != null) {
            coupon.setUsageLimit(request.getUsageLimit());
        }
        if (request.getExpiresAt() != null) {
            coupon.setExpiresAt(request.getExpiresAt());
        }
        if (request.getIsActive() != null) {
            coupon.setIsActive(request.getIsActive());
        }

        coupon = couponRepository.save(coupon);
        return toResponse(coupon);
    }

    public CouponResponse getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        return toResponse(coupon);
    }

    public CouponResponse getCouponByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Coupon code is required");
        }
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(code.toUpperCase())
                .orElseThrow(() -> new BadRequestException("Invalid or inactive coupon code"));
        
        // Additional runtime validation
        if (!coupon.isApplicable()) {
            throw new BadRequestException("Coupon is not applicable (expired, deleted, or usage limit reached)");
        }
        return toResponse(coupon);
    }

    public PagedResponse<CouponResponse> getAllCoupons(Boolean activeOnly, int page, int size) {
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Coupon> couponPage;
        
        if (Boolean.TRUE.equals(activeOnly)) {
            couponPage = couponRepository.findByIsActiveTrueAndDeletedFalseOrderByCreatedAtDesc(pageable);
        } else {
            couponPage = couponRepository.findByDeletedFalseOrderByCreatedAtDesc(pageable);
        }
        
        return PagedResponse.<CouponResponse>builder()
                .items(couponPage.getContent().stream().map(this::toResponse).toList())
                .total(couponPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(couponPage.getTotalPages())
                .build();
    }

    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        
        // Soft delete for audit trail
        coupon.setDeleted(true);
        coupon.setDeletedAt(LocalDateTime.now());
        coupon.setIsActive(false);
        couponRepository.save(coupon);
    }

    /**
     * Validate coupon request data with security constraints.
     */
    private void validateCouponRequest(CouponRequest request, DiscountType existingType) {
        if (request == null) {
            throw new BadRequestException("Coupon request cannot be null");
        }
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new BadRequestException("Coupon code is required");
        }
        if (request.getValue() == null) {
            throw new BadRequestException("Coupon value is required");
        }
        if (request.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Coupon value must be greater than zero");
        }

        // Determine effective discount type for validation
        DiscountType effectiveType = request.getDiscountType() != null 
                ? request.getDiscountType() 
                : existingType;
        
        if (effectiveType == null) {
            throw new BadRequestException("Discount type is required");
        }

        // Security: PERCENTAGE type cannot exceed 100%
        if (effectiveType == DiscountType.PERCENTAGE && request.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException("Percentage discount cannot exceed 100");
        }

        // Security: maxDiscountAmount must be >= value for FIXED type, or sensible for PERCENTAGE
        if (request.getMaxDiscountAmount() != null) {
            if (request.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Max discount amount cannot be negative");
            }
            if (effectiveType == DiscountType.FIXED && request.getMaxDiscountAmount().compareTo(request.getValue()) < 0) {
                throw new BadRequestException("Max discount cannot be less than coupon value for FIXED type");
            }
        }

        // Security: minOrderAmount cannot be negative
        if (request.getMinOrderAmount() != null && request.getMinOrderAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Minimum order amount cannot be negative");
        }

        // Security: usageLimit cannot be negative
        if (request.getUsageLimit() != null && request.getUsageLimit() < 0) {
            throw new BadRequestException("Usage limit cannot be negative");
        }

        // Security: expiry cannot be in the past for new coupons
        if (request.getExpiresAt() != null && request.getExpiresAt().isBefore(LocalDateTime.now()) 
                && existingType == null) { // Only check for new coupons
            throw new BadRequestException("Coupon expiry cannot be in the past");
        }
    }

    private CouponResponse toResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .value(coupon.getValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .usageLimit(coupon.getUsageLimit())
                .usageCount(coupon.getUsageCount())
                .isActive(coupon.getIsActive())
                .expiresAt(coupon.getExpiresAt())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }
}
