CREATE TABLE IF NOT EXISTS currencies (
    code CHAR(3) PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    symbol VARCHAR(8),
    fraction_digits SMALLINT NOT NULL DEFAULT 2,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_currencies_fraction_digits_non_negative CHECK (fraction_digits >= 0)
);

INSERT INTO currencies (code, name, symbol, fraction_digits, active)
VALUES
    ('USD', 'US Dollar', '$', 2, TRUE),
    ('EUR', 'Euro', 'EUR', 2, TRUE),
    ('JPY', 'Japanese Yen', 'JPY', 0, TRUE),
    ('KWD', 'Kuwaiti Dinar', 'KWD', 3, TRUE),
    ('BHD', 'Bahraini Dinar', 'BHD', 3, TRUE)
ON CONFLICT (code) DO NOTHING;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM prices p
        LEFT JOIN currencies c ON c.code = p.currency
        WHERE c.code IS NULL
    ) THEN
        RAISE EXCEPTION 'Unsupported currency codes exist in prices. Seed them in currencies before running V13.';
    END IF;
END $$;

ALTER TABLE prices
    ADD COLUMN IF NOT EXISTS amount_minor BIGINT;

UPDATE prices p
SET amount_minor = FLOOR((p.amount * POWER(10, c.fraction_digits)) + 0.5)::BIGINT
FROM currencies c
WHERE c.code = p.currency
  AND p.amount_minor IS NULL;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM prices WHERE amount_minor IS NULL) THEN
        RAISE EXCEPTION 'Unable to backfill prices.amount_minor for all rows.';
    END IF;
END $$;

ALTER TABLE prices
    ALTER COLUMN amount_minor SET NOT NULL;

ALTER TABLE prices
    ADD CONSTRAINT fk_prices_currency
    FOREIGN KEY (currency) REFERENCES currencies(code);

ALTER TABLE prices
    ADD CONSTRAINT chk_price_amount_minor_non_negative
    CHECK (amount_minor >= 0);
