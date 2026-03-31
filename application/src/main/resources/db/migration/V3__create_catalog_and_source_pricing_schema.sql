CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(140) NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS sources (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    system_managed BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(180) NOT NULL,
    slug VARCHAR(200) NOT NULL UNIQUE,
    description TEXT,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    main_image_url VARCHAR(255),
    ingredients TEXT,
    allergen_info TEXT,
    nutrition_facts TEXT,
    current_price_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS product_sources (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    source_id BIGINT NOT NULL REFERENCES sources(id) ON DELETE CASCADE,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    low_stock_threshold INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    price_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_product_source UNIQUE (product_id, source_id),
    CONSTRAINT chk_product_source_stock_non_negative CHECK (stock_quantity >= 0),
    CONSTRAINT chk_product_source_threshold_non_negative CHECK (low_stock_threshold >= 0)
);

CREATE TABLE IF NOT EXISTS prices (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    source_id BIGINT REFERENCES sources(id) ON DELETE CASCADE,
    amount NUMERIC(12, 2) NOT NULL,
    currency CHAR(3) NOT NULL,
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL,
    valid_to TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by BIGINT,
    CONSTRAINT chk_price_amount_positive CHECK (amount >= 0),
    CONSTRAINT chk_price_valid_range CHECK (valid_to IS NULL OR valid_to > valid_from),
    CONSTRAINT fk_prices_product_source_pair
        FOREIGN KEY (product_id, source_id)
        REFERENCES product_sources(product_id, source_id)
);

ALTER TABLE products
    ADD CONSTRAINT fk_products_current_price
    FOREIGN KEY (current_price_id) REFERENCES prices(id);

ALTER TABLE product_sources
    ADD CONSTRAINT fk_product_sources_price
    FOREIGN KEY (price_id) REFERENCES prices(id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_prices_open_interval_base
    ON prices (product_id, currency)
    WHERE source_id IS NULL AND valid_to IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_prices_open_interval_source
    ON prices (product_id, source_id, currency)
    WHERE source_id IS NOT NULL AND valid_to IS NULL;

CREATE INDEX IF NOT EXISTS idx_categories_active_sort ON categories (active, sort_order);
CREATE INDEX IF NOT EXISTS idx_products_category ON products (category_id);
CREATE INDEX IF NOT EXISTS idx_products_active_visible ON products (active, visible);
CREATE INDEX IF NOT EXISTS idx_products_name ON products (name);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products (sku);
CREATE INDEX IF NOT EXISTS idx_product_sources_source ON product_sources (source_id);
CREATE INDEX IF NOT EXISTS idx_product_sources_status ON product_sources (status);
CREATE INDEX IF NOT EXISTS idx_prices_product_lookup ON prices (product_id, currency, valid_from DESC);
CREATE INDEX IF NOT EXISTS idx_prices_source_lookup ON prices (product_id, source_id, currency, valid_from DESC);

CREATE OR REPLACE FUNCTION prevent_mutation_of_system_sources()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF OLD.system_managed THEN
        RAISE EXCEPTION 'System managed source % cannot be modified or deleted', OLD.code;
    END IF;

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_prevent_system_source_update_delete ON sources;

CREATE TRIGGER trg_prevent_system_source_update_delete
BEFORE UPDATE OR DELETE ON sources
FOR EACH ROW
EXECUTE FUNCTION prevent_mutation_of_system_sources();
