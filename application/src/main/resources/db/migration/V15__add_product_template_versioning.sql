ALTER TABLE product_templates
    ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS is_latest BOOLEAN NOT NULL DEFAULT TRUE;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'product_templates_code_key'
    ) THEN
        ALTER TABLE product_templates DROP CONSTRAINT product_templates_code_key;
    END IF;
END
$$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_product_templates_code_version
    ON product_templates (code, version);

CREATE UNIQUE INDEX IF NOT EXISTS uk_product_templates_code_latest
    ON product_templates (code)
    WHERE is_latest = true;

CREATE INDEX IF NOT EXISTS idx_product_templates_latest
    ON product_templates (is_latest);
