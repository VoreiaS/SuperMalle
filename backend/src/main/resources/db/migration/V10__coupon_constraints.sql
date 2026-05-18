-- QA production-readiness: Coupon constraints + soft-delete
ALTER TABLE coupons ADD CONSTRAINT uk_coupon_code UNIQUE (code);
ALTER TABLE coupons ADD CONSTRAINT chk_discount_type CHECK (discount_type IN ('PERCENTAGE', 'FIXED'));
ALTER TABLE coupons ADD COLUMN deleted_at TIMESTAMP NULL;
CREATE INDEX idx_coupons_deleted_at ON coupons(deleted_at);
