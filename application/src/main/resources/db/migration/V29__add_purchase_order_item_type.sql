ALTER TABLE admin_source_purchase_order_items
    ADD COLUMN IF NOT EXISTS item_type VARCHAR(30);

UPDATE admin_source_purchase_order_items
SET item_type = CASE
    WHEN product_id IS NULL THEN 'AD_HOC_PRODUCT'
    ELSE 'CATALOG_PRODUCT'
END
WHERE item_type IS NULL;

ALTER TABLE admin_source_purchase_order_items
    ALTER COLUMN item_type SET NOT NULL;

DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_admin_source_purchase_order_items_item_type'
    ) THEN
        ALTER TABLE admin_source_purchase_order_items
            ADD CONSTRAINT chk_admin_source_purchase_order_items_item_type
            CHECK (item_type IN ('CATALOG_PRODUCT', 'AD_HOC_PRODUCT'));
    END IF;
END
$$;
