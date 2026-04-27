ALTER TABLE admin_source_stock_movements
    ADD COLUMN IF NOT EXISTS transfer_id BIGINT REFERENCES admin_source_transfers(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_admin_source_stock_movements_transfer_created_at
    ON admin_source_stock_movements (transfer_id, created_at DESC);
