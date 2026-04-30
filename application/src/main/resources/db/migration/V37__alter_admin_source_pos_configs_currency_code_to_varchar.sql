ALTER TABLE admin_source_pos_configs
    ALTER COLUMN default_currency_code TYPE VARCHAR(3)
    USING NULLIF(BTRIM(default_currency_code), '');
