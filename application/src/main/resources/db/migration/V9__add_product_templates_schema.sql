CREATE TABLE IF NOT EXISTS product_templates (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(180) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_product_templates_active ON product_templates (active);
CREATE INDEX IF NOT EXISTS idx_product_templates_code ON product_templates (code);

CREATE TABLE IF NOT EXISTS product_template_fields (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES product_templates(id) ON DELETE CASCADE,
    field_key VARCHAR(120) NOT NULL,
    label VARCHAR(120) NOT NULL,
    field_type VARCHAR(30) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    default_value TEXT,
    validation_rules TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_product_template_fields_template_key UNIQUE (template_id, field_key),
    CONSTRAINT chk_product_template_fields_field_type CHECK (
        field_type IN ('TEXT', 'LONG_TEXT', 'NUMBER', 'BOOLEAN', 'DATE', 'DATETIME', 'SELECT', 'MULTISELECT')
    )
);

CREATE INDEX IF NOT EXISTS idx_product_template_fields_template_sort
    ON product_template_fields (template_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_product_template_fields_field_type
    ON product_template_fields (field_type);

ALTER TABLE categories
    ADD COLUMN IF NOT EXISTS default_template_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_categories_default_template'
    ) THEN
        ALTER TABLE categories
            ADD CONSTRAINT fk_categories_default_template
            FOREIGN KEY (default_template_id)
            REFERENCES product_templates(id)
            ON DELETE SET NULL;
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_categories_default_template_id
    ON categories (default_template_id);

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS template_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_products_template'
    ) THEN
        ALTER TABLE products
            ADD CONSTRAINT fk_products_template
            FOREIGN KEY (template_id)
            REFERENCES product_templates(id)
            ON DELETE SET NULL;
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_products_template_id
    ON products (template_id);
