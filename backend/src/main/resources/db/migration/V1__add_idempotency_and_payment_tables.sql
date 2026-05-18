-- V1__add_idempotency_and_payment_tables.sql
-- Initial schema migration for production readiness
-- Generated: 2025-01-09

-- Idempotency key tracking table (prevents duplicate request processing)
CREATE TABLE IF NOT EXISTS idempotency_keys (
    id BIGSERIAL PRIMARY KEY,
    key VARCHAR(255) NOT NULL,
    entity VARCHAR(50) NOT NULL,
    user_id BIGINT,
    request_hash VARCHAR(64) NOT NULL,
    response_body TEXT,
    response_status INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    error_message TEXT,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Unique index prevents duplicate keys per user/entity (atomic check)
    CONSTRAINT uk_idempotency_key UNIQUE (key, entity, user_id)
);

-- Index for cleanup job (find expired records efficiently)
CREATE INDEX IF NOT EXISTS idx_idempotency_cleanup ON idempotency_keys (expires_at);
CREATE INDEX IF NOT EXISTS idx_idempotency_lookup ON idempotency_keys (key, entity, user_id);

-- Enhance Payment table with additional security/audit fields
ALTER TABLE payment 
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(255),
    ADD COLUMN IF NOT EXISTS client_ip VARCHAR(45),
    ADD COLUMN IF NOT EXISTS user_agent TEXT,
    ADD COLUMN IF NOT EXISTS processed_at TIMESTAMP;

-- Index for idempotency key lookups on payments
CREATE INDEX IF NOT EXISTS idx_payment_idempotency_key ON payment (idempotency_key) WHERE idempotency_key IS NOT NULL;

-- Add optimistic locking version column to menu_item (prevents oversell race conditions)
ALTER TABLE menu_item 
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Add soft-delete flag to menu_item (prevents broken references when items are "deleted")
ALTER TABLE menu_item 
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Index for filtering soft-deleted items
CREATE INDEX IF NOT EXISTS idx_menu_item_active ON menu_item (deleted) WHERE deleted = FALSE;

-- Add unique constraint to coupon code + active status (prevents duplicate active codes)
ALTER TABLE coupon 
    ADD CONSTRAINT uk_coupon_code_active UNIQUE (code, is_active);

-- Add audit fields to order for compliance tracking
ALTER TABLE "order"
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255);

-- Add index for order status filtering (improves admin dashboard performance)
CREATE INDEX IF NOT EXISTS idx_order_status_created ON "order" (status, created_at);
