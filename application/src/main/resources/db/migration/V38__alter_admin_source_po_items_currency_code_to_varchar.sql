ALTER TABLE admin_source_purchase_order_items
    ALTER COLUMN currency_code TYPE VARCHAR(3)
    USING NULLIF(BTRIM(currency_code), '');
