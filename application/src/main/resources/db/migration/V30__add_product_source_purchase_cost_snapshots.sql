ALTER TABLE product_sources
    ADD COLUMN IF NOT EXISTS last_purchase_cost_minor BIGINT,
    ADD COLUMN IF NOT EXISTS average_purchase_cost_minor BIGINT,
    ADD COLUMN IF NOT EXISTS last_purchase_at TIMESTAMP WITH TIME ZONE;
