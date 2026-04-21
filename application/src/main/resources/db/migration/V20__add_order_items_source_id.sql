ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS source_id BIGINT REFERENCES sources(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_order_items_source_id ON order_items (source_id);
