CREATE TABLE IF NOT EXISTS package_option_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    description TEXT,
    uses_selection_bounds BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO package_option_types (
    code,
    name,
    description,
    uses_selection_bounds,
    created_at,
    updated_at
)
VALUES
    ('ALL_OF', 'All Of', 'All items in the option are selected by default', FALSE, NOW(), NOW()),
    ('ONE_OF', 'One Of', 'Exactly one item can be selected', TRUE, NOW(), NOW()),
    ('N_OF', 'N Of', 'A fixed or bounded number of items can be selected', TRUE, NOW(), NOW())
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    uses_selection_bounds = EXCLUDED.uses_selection_bounds,
    updated_at = NOW();

CREATE TABLE IF NOT EXISTS package_options (
    id BIGSERIAL PRIMARY KEY,
    package_product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    option_type_id BIGINT NOT NULL REFERENCES package_option_types(id),
    category_id BIGINT REFERENCES categories(id),
    name VARCHAR(120) NOT NULL,
    min_select INTEGER,
    max_select INTEGER,
    sort_order INTEGER NOT NULL DEFAULT 0,
    required BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_package_options_min_select_non_negative CHECK (min_select IS NULL OR min_select >= 0),
    CONSTRAINT chk_package_options_max_select_non_negative CHECK (max_select IS NULL OR max_select >= 0),
    CONSTRAINT chk_package_options_select_range CHECK (
        min_select IS NULL OR max_select IS NULL OR max_select >= min_select
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_package_options_product_name
    ON package_options (package_product_id, name);
CREATE INDEX IF NOT EXISTS idx_package_options_product_sort
    ON package_options (package_product_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_package_options_option_type
    ON package_options (option_type_id);
CREATE INDEX IF NOT EXISTS idx_package_options_category
    ON package_options (category_id);

CREATE TABLE IF NOT EXISTS package_option_items (
    id BIGSERIAL PRIMARY KEY,
    package_option_id BIGINT NOT NULL REFERENCES package_options(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    source_id BIGINT REFERENCES sources(id) ON DELETE CASCADE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    extra_price_mode VARCHAR(30) NOT NULL DEFAULT 'NO_EXTRA',
    extra_price NUMERIC(12, 2),
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_package_option_items_extra_price_mode CHECK (
        extra_price_mode IN ('NO_EXTRA', 'FIXED_EXTRA')
    ),
    CONSTRAINT chk_package_option_items_extra_price_rule CHECK (
        (extra_price_mode = 'NO_EXTRA' AND extra_price IS NULL)
        OR
        (extra_price_mode = 'FIXED_EXTRA' AND extra_price IS NOT NULL AND extra_price >= 0)
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_package_option_items_option_product_source
    ON package_option_items (package_option_id, product_id, COALESCE(source_id, -1));
CREATE INDEX IF NOT EXISTS idx_package_option_items_option_sort
    ON package_option_items (package_option_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_package_option_items_product
    ON package_option_items (product_id);
CREATE INDEX IF NOT EXISTS idx_package_option_items_source
    ON package_option_items (source_id);

CREATE OR REPLACE FUNCTION enforce_package_option_item_category_match()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    option_category_id BIGINT;
    item_product_category_id BIGINT;
BEGIN
    SELECT po.category_id, p.category_id
      INTO option_category_id, item_product_category_id
      FROM package_options po
      JOIN products p ON p.id = NEW.product_id
     WHERE po.id = NEW.package_option_id;

    IF option_category_id IS NOT NULL AND option_category_id <> item_product_category_id THEN
        RAISE EXCEPTION
            'Product % category % is not allowed for package option % category %',
            NEW.product_id, item_product_category_id, NEW.package_option_id, option_category_id;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_enforce_package_option_item_category_match ON package_option_items;

CREATE TRIGGER trg_enforce_package_option_item_category_match
BEFORE INSERT OR UPDATE OF package_option_id, product_id
ON package_option_items
FOR EACH ROW
EXECUTE FUNCTION enforce_package_option_item_category_match();

CREATE OR REPLACE FUNCTION enforce_package_option_category_update()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.category_id IS NULL THEN
        RETURN NEW;
    END IF;

    IF EXISTS (
        SELECT 1
          FROM package_option_items poi
          JOIN products p ON p.id = poi.product_id
         WHERE poi.package_option_id = NEW.id
           AND p.category_id <> NEW.category_id
    ) THEN
        RAISE EXCEPTION
            'Cannot assign category % to package option % because existing items do not match',
            NEW.category_id, NEW.id;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_enforce_package_option_category_update ON package_options;

CREATE TRIGGER trg_enforce_package_option_category_update
BEFORE UPDATE OF category_id
ON package_options
FOR EACH ROW
EXECUTE FUNCTION enforce_package_option_category_update();
