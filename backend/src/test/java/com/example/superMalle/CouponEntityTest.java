package com.example.superMalle;

import com.example.superMalle.entity.Coupon;
import com.example.superMalle.entity.enums.DiscountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to prove Coupon entity works correctly.
 * This test demonstrates that all fields and methods exist and work as expected.
 */
class CouponEntityTest {

    @Test
    void testCouponEntityFieldsExist() {
        // Create a coupon using builder
        Coupon coupon = Coupon.builder()
                .id(1L)
                .code("TEST10")
                .discountType(DiscountType.PERCENTAGE)
                .value(new BigDecimal("10"))
                .minOrderAmount(new BigDecimal("50"))
                .maxDiscountAmount(new BigDecimal("20"))
                .usageLimit(100)
                .usageCount(0)
                .isActive(true)
                .deleted(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Verify all fields exist and are set correctly
        assertNotNull(coupon);
        assertEquals(1L, coupon.getId());
        assertEquals("TEST10", coupon.getCode());
        assertEquals(DiscountType.PERCENTAGE, coupon.getDiscountType());
        assertEquals(new BigDecimal("10"), coupon.getValue());
        assertEquals(new BigDecimal("50"), coupon.getMinOrderAmount());
        assertEquals(new BigDecimal("20"), coupon.getMaxDiscountAmount());
        assertEquals(100, coupon.getUsageLimit());
        assertEquals(0, coupon.getUsageCount());
        assertTrue(coupon.getIsActive());
        assertFalse(coupon.getDeleted());
        assertNotNull(coupon.getExpiresAt());
        assertNotNull(coupon.getCreatedAt());
        assertNotNull(coupon.getUpdatedAt());
    }

    @Test
    void testCouponEntitySettersWork() {
        // Create a coupon using no-args constructor
        Coupon coupon = new Coupon();

        // Test all setters work
        coupon.setId(1L);
        coupon.setCode("TEST20");
        coupon.setDiscountType(DiscountType.FIXED);
        coupon.setValue(new BigDecimal("20"));
        coupon.setMinOrderAmount(new BigDecimal("100"));
        coupon.setMaxDiscountAmount(new BigDecimal("50"));
        coupon.setUsageLimit(200);
        coupon.setUsageCount(5);
        coupon.setIsActive(true);
        coupon.setDeleted(false);
        coupon.setExpiresAt(LocalDateTime.now().plusDays(60));
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUpdatedAt(LocalDateTime.now());

        // Verify all getters work
        assertEquals(1L, coupon.getId());
        assertEquals("TEST20", coupon.getCode());
        assertEquals(DiscountType.FIXED, coupon.getDiscountType());
        assertEquals(new BigDecimal("20"), coupon.getValue());
        assertEquals(new BigDecimal("100"), coupon.getMinOrderAmount());
        assertEquals(new BigDecimal("50"), coupon.getMaxDiscountAmount());
        assertEquals(200, coupon.getUsageLimit());
        assertEquals(5, coupon.getUsageCount());
        assertTrue(coupon.getIsActive());
        assertFalse(coupon.getDeleted());
        assertNotNull(coupon.getExpiresAt());
        assertNotNull(coupon.getCreatedAt());
        assertNotNull(coupon.getUpdatedAt());
    }

    @Test
    void testCouponEntityIsDeletedMethod() {
        Coupon coupon = new Coupon();

        // Test isDeleted() method
        coupon.setDeleted(false);
        assertFalse(coupon.isDeleted());

        coupon.setDeleted(true);
        assertTrue(coupon.isDeleted());

        coupon.setDeleted(null);
        assertFalse(coupon.isDeleted());
    }

    @Test
    void testCouponEntityCalculateDiscountPercentage() {
        Coupon coupon = Coupon.builder()
                .code("PERCENT10")
                .discountType(DiscountType.PERCENTAGE)
                .value(new BigDecimal("10"))
                .minOrderAmount(new BigDecimal("50"))
                .maxDiscountAmount(new BigDecimal("20"))
                .isActive(true)
                .deleted(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        // Test percentage discount
        BigDecimal subtotal = new BigDecimal("100");
        BigDecimal discount = coupon.calculateDiscount(subtotal);

        // 10% of 100 = 10
        assertEquals(0, discount.compareTo(new BigDecimal("10")));
    }

    @Test
    void testCouponEntityCalculateDiscountFixed() {
        Coupon coupon = Coupon.builder()
                .code("FIXED20")
                .discountType(DiscountType.FIXED)
                .value(new BigDecimal("20"))
                .minOrderAmount(new BigDecimal("50"))
                .isActive(true)
                .deleted(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        // Test fixed discount
        BigDecimal subtotal = new BigDecimal("100");
        BigDecimal discount = coupon.calculateDiscount(subtotal);

        // Fixed discount of 20
        assertEquals(0, discount.compareTo(new BigDecimal("20")));
    }

    @Test
    void testCouponEntityIsApplicable() {
        Coupon coupon = Coupon.builder()
                .code("APPLICABLE")
                .discountType(DiscountType.PERCENTAGE)
                .value(new BigDecimal("10"))
                .isActive(true)
                .deleted(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .usageLimit(100)
                .usageCount(50)
                .build();

        // Test isApplicable() method
        assertTrue(coupon.isApplicable());

        // Test with inactive coupon
        coupon.setIsActive(false);
        assertFalse(coupon.isApplicable());

        // Test with deleted coupon
        coupon.setIsActive(true);
        coupon.setDeleted(true);
        assertFalse(coupon.isApplicable());

        // Test with expired coupon
        coupon.setDeleted(false);
        coupon.setExpiresAt(LocalDateTime.now().minusDays(1));
        assertFalse(coupon.isApplicable());

        // Test with usage limit exceeded
        coupon.setExpiresAt(LocalDateTime.now().plusDays(30));
        coupon.setUsageCount(100);
        assertFalse(coupon.isApplicable());
    }

    @Test
    void testDiscountTypeEnumValues() {
        // Test DiscountType enum values exist
        assertNotNull(DiscountType.PERCENTAGE);
        assertNotNull(DiscountType.FIXED);

        // Test enum comparison works
        assertEquals(DiscountType.PERCENTAGE, DiscountType.PERCENTAGE);
        assertEquals(DiscountType.FIXED, DiscountType.FIXED);
        assertNotEquals(DiscountType.PERCENTAGE, DiscountType.FIXED);
    }
}
