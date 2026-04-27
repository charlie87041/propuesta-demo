CREATE INDEX IF NOT EXISTS idx_admin_source_transfer_items_product_transfer
    ON admin_source_transfer_items (product_id, transfer_id);
