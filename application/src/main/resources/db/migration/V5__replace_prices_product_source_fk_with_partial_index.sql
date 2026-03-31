ALTER TABLE prices
    DROP CONSTRAINT IF EXISTS fk_prices_product_source_pair;

CREATE INDEX IF NOT EXISTS idx_prices_product_source_pair_not_null
    ON prices (product_id, source_id)
    WHERE source_id IS NOT NULL;
