ALTER TABLE customer_addresses
    ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS is_latest BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS previous_version_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_customer_addresses_version_positive'
    ) THEN
        ALTER TABLE customer_addresses
            ADD CONSTRAINT chk_customer_addresses_version_positive
            CHECK (version >= 1);
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_customer_addresses_previous_version'
    ) THEN
        ALTER TABLE customer_addresses
            ADD CONSTRAINT fk_customer_addresses_previous_version
            FOREIGN KEY (previous_version_id)
            REFERENCES customer_addresses(id)
            ON DELETE SET NULL;
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_customer_addresses_customer_latest
    ON customer_addresses (customer_id, is_latest);

CREATE INDEX IF NOT EXISTS idx_customer_addresses_previous_version_id
    ON customer_addresses (previous_version_id);
