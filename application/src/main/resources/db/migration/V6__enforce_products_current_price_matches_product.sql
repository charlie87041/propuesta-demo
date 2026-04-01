-- Ensure products.current_price_id (when present) points to a price row of the same product.

UPDATE products p
SET current_price_id = NULL
WHERE current_price_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM prices pr
    WHERE pr.id = p.current_price_id
      AND pr.product_id = p.id
  );

ALTER TABLE products
    DROP CONSTRAINT IF EXISTS fk_products_current_price;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_prices_product_id_id'
    ) THEN
        ALTER TABLE prices
            ADD CONSTRAINT uk_prices_product_id_id UNIQUE (product_id, id);
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_products_current_price_same_product'
    ) THEN
        ALTER TABLE products
            ADD CONSTRAINT fk_products_current_price_same_product
            FOREIGN KEY (id, current_price_id)
            REFERENCES prices (product_id, id);
    END IF;
END
$$;
