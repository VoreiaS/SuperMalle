package com.example.superMalle.controller;

import com.example.superMalle.dto.admin.CouponResponse;
import com.example.superMalle.dto.coupon.CouponValidationResponse;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/validate")
    public ResponseEntity<CouponValidationResponse> validateCoupon(@RequestParam String code) {
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(
                CouponValidationResponse.builder()
                    .valid(false)
                    .message("Coupon code is required")
                    .build()
            );
        }
        try {
            CouponResponse coupon = couponService.getCouponByCode(code);
            return ResponseEntity.ok(CouponValidationResponse.builder()
                .valid(true)
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .value(coupon.getValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .message("Coupon is valid")
                .build());
        } catch (BadRequestException e) {
            return ResponseEntity.ok(CouponValidationResponse.builder()
                .valid(false)
                .code(code)
                .message(e.getMessage())
                .build());
        }
    }
}
