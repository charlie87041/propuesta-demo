CREATE TABLE IF NOT EXISTS product_types (
    code VARCHAR(30) PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    description TEXT,
    supports_components BOOLEAN NOT NULL DEFAULT FALSE,
    supports_variants BOOLEAN NOT NULL DEFAULT FALSE,
    supports_addons BOOLEAN NOT NULL DEFAULT FALSE,
    default_is_listable BOOLEAN NOT NULL DEFAULT TRUE,
    default_is_searchable BOOLEAN NOT NULL DEFAULT TRUE,
    default_is_purchasable_alone BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO product_types (
    code,
    name,
    description,
    supports_components,
    supports_variants,
    supports_addons,
    default_is_listable,
    default_is_searchable,
    default_is_purchasable_alone,
    created_at,
    updated_at
)
VALUES
    ('SIMPLE', 'Simple', 'Standalone sellable product', FALSE, FALSE, TRUE, TRUE, TRUE, TRUE, NOW(), NOW()),
    ('BUNDLE', 'Bundle', 'Composable product made of multiple child products', TRUE, FALSE, TRUE, TRUE, TRUE, TRUE, NOW(), NOW()),
    ('PACKAGE', 'Package', 'Fixed commercial pack sold as one unit', TRUE, FALSE, TRUE, TRUE, TRUE, TRUE, NOW(), NOW()),
    ('VARIANT_PARENT', 'Variant Parent', 'Non-sellable parent that groups variants', FALSE, TRUE, FALSE, TRUE, TRUE, FALSE, NOW(), NOW()),
    ('VARIANT', 'Variant', 'Sellable variant linked to a variant parent', FALSE, FALSE, TRUE, FALSE, FALSE, TRUE, NOW(), NOW()),
    ('ADD_ON', 'Add On', 'Complementary product that is not sold on its own by default', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, NOW(), NOW())
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    supports_components = EXCLUDED.supports_components,
    supports_variants = EXCLUDED.supports_variants,
    supports_addons = EXCLUDED.supports_addons,
    default_is_listable = EXCLUDED.default_is_listable,
    default_is_searchable = EXCLUDED.default_is_searchable,
    default_is_purchasable_alone = EXCLUDED.default_is_purchasable_alone,
    updated_at = NOW();

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS product_type_code VARCHAR(30) NOT NULL DEFAULT 'SIMPLE',
    ADD COLUMN IF NOT EXISTS is_listable BOOLEAN,
    ADD COLUMN IF NOT EXISTS is_searchable BOOLEAN,
    ADD COLUMN IF NOT EXISTS is_purchasable_alone BOOLEAN;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_products_product_type'
    ) THEN
        ALTER TABLE products
            ADD CONSTRAINT fk_products_product_type
            FOREIGN KEY (product_type_code) REFERENCES product_types(code);
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_products_product_type_code ON products (product_type_code);

CREATE TABLE IF NOT EXISTS product_variants (
    id BIGSERIAL PRIMARY KEY,
    parent_product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_product_variants_parent_variant UNIQUE (parent_product_id, variant_product_id),
    CONSTRAINT uk_product_variants_variant UNIQUE (variant_product_id),
    CONSTRAINT chk_product_variants_not_self CHECK (parent_product_id <> variant_product_id)
);

CREATE INDEX IF NOT EXISTS idx_product_variants_parent_sort ON product_variants (parent_product_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_product_variants_variant ON product_variants (variant_product_id);

CREATE TABLE IF NOT EXISTS product_components (
    id BIGSERIAL PRIMARY KEY,
    parent_product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    child_product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    source_id BIGINT REFERENCES sources(id) ON DELETE CASCADE,
    quantity NUMERIC(12, 3) NOT NULL DEFAULT 1,
    unit_price_mode VARCHAR(30) NOT NULL DEFAULT 'INHERIT_PRODUCT_PRICE',
    unit_price_override NUMERIC(12, 2),
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_product_components_not_self CHECK (parent_product_id <> child_product_id),
    CONSTRAINT chk_product_components_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_product_components_price_mode CHECK (unit_price_mode IN ('INHERIT_PRODUCT_PRICE', 'FIXED_OVERRIDE')),
    CONSTRAINT chk_product_components_price_override_rule CHECK (
        (unit_price_mode = 'FIXED_OVERRIDE' AND unit_price_override IS NOT NULL AND unit_price_override >= 0)
        OR
        (unit_price_mode = 'INHERIT_PRODUCT_PRICE' AND unit_price_override IS NULL)
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_product_components_parent_child_source
    ON product_components (parent_product_id, child_product_id, COALESCE(source_id, -1));
CREATE INDEX IF NOT EXISTS idx_product_components_parent_sort ON product_components (parent_product_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_product_components_child ON product_components (child_product_id);
CREATE INDEX IF NOT EXISTS idx_product_components_source ON product_components (source_id);

CREATE TABLE IF NOT EXISTS product_addons (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    addon_product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    source_id BIGINT REFERENCES sources(id) ON DELETE CASCADE,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    min_qty NUMERIC(12, 3) NOT NULL DEFAULT 0,
    max_qty NUMERIC(12, 3),
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_product_addons_not_self CHECK (product_id <> addon_product_id),
    CONSTRAINT chk_product_addons_min_qty_non_negative CHECK (min_qty >= 0),
    CONSTRAINT chk_product_addons_max_qty_rule CHECK (max_qty IS NULL OR max_qty >= min_qty)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_product_addons_product_addon_source
    ON product_addons (product_id, addon_product_id, COALESCE(source_id, -1));
CREATE INDEX IF NOT EXISTS idx_product_addons_product_sort ON product_addons (product_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_product_addons_addon ON product_addons (addon_product_id);
CREATE INDEX IF NOT EXISTS idx_product_addons_source ON product_addons (source_id);
