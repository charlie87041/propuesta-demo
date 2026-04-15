CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    increment_id VARCHAR(40) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    is_guest BOOLEAN NOT NULL DEFAULT FALSE,
    customer_id BIGINT REFERENCES customers(id) ON DELETE SET NULL,
    customer_email VARCHAR(255),
    customer_first_name VARCHAR(120),
    customer_last_name VARCHAR(120),
    coupon_code VARCHAR(100),
    total_item_count INTEGER NOT NULL DEFAULT 0,
    total_qty_ordered INTEGER NOT NULL DEFAULT 0,
    order_currency_code CHAR(3) NOT NULL REFERENCES currencies(code),
    sub_total_minor BIGINT NOT NULL DEFAULT 0,
    discount_total_minor BIGINT NOT NULL DEFAULT 0,
    tax_total_minor BIGINT NOT NULL DEFAULT 0,
    shipping_total_minor BIGINT NOT NULL DEFAULT 0,
    grand_total_minor BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_orders_total_item_count_non_negative CHECK (total_item_count >= 0),
    CONSTRAINT chk_orders_total_qty_ordered_non_negative CHECK (total_qty_ordered >= 0),
    CONSTRAINT chk_orders_sub_total_minor_non_negative CHECK (sub_total_minor >= 0),
    CONSTRAINT chk_orders_discount_total_minor_non_negative CHECK (discount_total_minor >= 0),
    CONSTRAINT chk_orders_tax_total_minor_non_negative CHECK (tax_total_minor >= 0),
    CONSTRAINT chk_orders_shipping_total_minor_non_negative CHECK (shipping_total_minor >= 0),
    CONSTRAINT chk_orders_grand_total_minor_non_negative CHECK (grand_total_minor >= 0)
);

CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders (customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders (status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders (created_at DESC);

CREATE TABLE IF NOT EXISTS order_addresses (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    address_type VARCHAR(20) NOT NULL,
    first_name VARCHAR(120) NOT NULL,
    last_name VARCHAR(120) NOT NULL,
    gender VARCHAR(40),
    company_name VARCHAR(180),
    address1 VARCHAR(255) NOT NULL,
    address2 VARCHAR(255),
    city VARCHAR(140) NOT NULL,
    state VARCHAR(140),
    country VARCHAR(140),
    postcode VARCHAR(32),
    email VARCHAR(255),
    phone VARCHAR(32),
    vat_id VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_order_addresses_type CHECK (address_type IN ('SHIPPING', 'BILLING')),
    CONSTRAINT uk_order_addresses_order_type UNIQUE (order_id, address_type)
);

CREATE INDEX IF NOT EXISTS idx_order_addresses_order_id ON order_addresses (order_id);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    parent_id BIGINT REFERENCES order_items(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id) ON DELETE SET NULL,
    sku VARCHAR(80),
    product_name VARCHAR(180) NOT NULL,
    product_type_code VARCHAR(30),
    quantity_ordered INTEGER NOT NULL DEFAULT 0,
    quantity_shipped INTEGER NOT NULL DEFAULT 0,
    quantity_invoiced INTEGER NOT NULL DEFAULT 0,
    quantity_canceled INTEGER NOT NULL DEFAULT 0,
    quantity_refunded INTEGER NOT NULL DEFAULT 0,
    unit_price_minor BIGINT NOT NULL DEFAULT 0,
    line_sub_total_minor BIGINT NOT NULL DEFAULT 0,
    line_discount_minor BIGINT NOT NULL DEFAULT 0,
    line_tax_minor BIGINT NOT NULL DEFAULT 0,
    line_total_minor BIGINT NOT NULL DEFAULT 0,
    currency_code CHAR(3) NOT NULL REFERENCES currencies(code),
    additional JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_order_items_quantity_ordered_positive CHECK (quantity_ordered > 0),
    CONSTRAINT chk_order_items_quantity_shipped_non_negative CHECK (quantity_shipped >= 0),
    CONSTRAINT chk_order_items_quantity_invoiced_non_negative CHECK (quantity_invoiced >= 0),
    CONSTRAINT chk_order_items_quantity_canceled_non_negative CHECK (quantity_canceled >= 0),
    CONSTRAINT chk_order_items_quantity_refunded_non_negative CHECK (quantity_refunded >= 0),
    CONSTRAINT chk_order_items_unit_price_minor_non_negative CHECK (unit_price_minor >= 0),
    CONSTRAINT chk_order_items_line_sub_total_minor_non_negative CHECK (line_sub_total_minor >= 0),
    CONSTRAINT chk_order_items_line_discount_minor_non_negative CHECK (line_discount_minor >= 0),
    CONSTRAINT chk_order_items_line_tax_minor_non_negative CHECK (line_tax_minor >= 0),
    CONSTRAINT chk_order_items_line_total_minor_non_negative CHECK (line_total_minor >= 0)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items (order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_parent_id ON order_items (parent_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items (product_id);

CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    note TEXT,
    changed_by_admin_user_id BIGINT REFERENCES admin_users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_order_status_history_order_id_created_at
    ON order_status_history (order_id, created_at DESC);
