-- Database Optimization Migration
-- Version: V11
-- Description: Add indexes for performance optimization

-- Indexes for foreign keys
CREATE INDEX IF NOT EXISTS idx_menu_items_category_id ON menu_items(category_id);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_menu_item_id ON order_items(menu_item_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_menu_item_id ON cart_items(menu_item_id);
CREATE INDEX IF NOT EXISTS idx_cart_user_id ON cart(user_id);
CREATE INDEX IF NOT EXISTS idx_inventory_menu_item_id ON inventory(menu_item_id);
CREATE INDEX IF NOT EXISTS idx_user_loyalty_user_id ON user_loyalty(user_id);
CREATE INDEX IF NOT EXISTS idx_user_loyalty_loyalty_program_id ON user_loyalty(loyalty_program_id);
CREATE INDEX IF NOT EXISTS idx_loyalty_transactions_user_loyalty_id ON loyalty_transactions(user_loyalty_id);
CREATE INDEX IF NOT EXISTS idx_order_modifications_order_id ON order_modifications(order_id);

-- Composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_menu_items_category_available ON menu_items(category_id, is_available);
CREATE INDEX IF NOT EXISTS idx_orders_user_status ON orders(user_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_status_created ON orders(status, created_at);
CREATE INDEX IF NOT EXISTS idx_inventory_quantity_reorder ON inventory(quantity, reorder_level);
CREATE INDEX IF NOT EXISTS idx_user_loyalty_points_tier ON user_loyalty(points_balance, tier);

-- Indexes for search and filtering
CREATE INDEX IF NOT EXISTS idx_menu_items_name ON menu_items(name);
CREATE INDEX IF NOT EXISTS idx_menu_items_price ON menu_items(price);
CREATE INDEX IF NOT EXISTS idx_menu_items_available ON menu_items(is_available);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_orders_total_amount ON orders(total_amount);

-- Indexes for audit and tracking
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_loyalty_transactions_created_at ON loyalty_transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_order_modifications_created_at ON order_modifications(created_at);

-- Partial indexes for better performance
CREATE INDEX IF NOT EXISTS idx_menu_items_available_only ON menu_items(id) WHERE is_available = true;
CREATE INDEX IF NOT EXISTS idx_inventory_low_stock ON inventory(id) WHERE quantity <= reorder_level;
CREATE INDEX IF NOT EXISTS idx_orders_pending ON orders(id) WHERE status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_orders_processing ON orders(id) WHERE status = 'PROCESSING';

-- Indexes for sorting and pagination
CREATE INDEX IF NOT EXISTS idx_menu_items_created_at ON menu_items(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_updated_at ON orders(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_inventory_updated_at ON inventory(updated_at DESC);

-- Comment on indexes for documentation
COMMENT ON INDEX idx_menu_items_category_id IS 'Index for menu items category lookups';
COMMENT ON INDEX idx_orders_user_id IS 'Index for user orders lookups';
COMMENT ON INDEX idx_menu_items_category_available IS 'Composite index for category and availability filtering';
COMMENT ON INDEX idx_orders_user_status IS 'Composite index for user orders by status';
COMMENT ON INDEX idx_menu_items_available_only IS 'Partial index for available menu items only';
COMMENT ON INDEX idx_inventory_low_stock IS 'Partial index for low stock items';
