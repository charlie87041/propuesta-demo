ALTER TABLE admin_source_pos_configs
    ALTER COLUMN default_currency_code TYPE CHAR(3)
    USING NULLIF(BTRIM(default_currency_code), '')::CHAR(3);

ALTER TABLE admin_source_purchase_order_items
    ALTER COLUMN currency_code TYPE CHAR(3)
    USING NULLIF(BTRIM(currency_code), '')::CHAR(3);

ALTER TABLE orders
    ALTER COLUMN order_currency_code TYPE CHAR(3)
    USING BTRIM(order_currency_code)::CHAR(3);

ALTER TABLE order_items
    ALTER COLUMN currency_code TYPE CHAR(3)
    USING BTRIM(currency_code)::CHAR(3);
