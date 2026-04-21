CREATE TABLE IF NOT EXISTS admin_source_stock_movements (
    id BIGSERIAL PRIMARY KEY,
    source_id BIGINT NOT NULL REFERENCES sources(id) ON DELETE CASCADE,
    order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id) ON DELETE SET NULL,
    movement_type VARCHAR(40) NOT NULL,
    quantity_delta INTEGER NOT NULL,
    balance_after INTEGER,
    reference_type VARCHAR(40),
    reference_code VARCHAR(80),
    note TEXT,
    metadata TEXT,
    created_by_admin_user_id BIGINT REFERENCES admin_users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_admin_source_stock_movements_type CHECK (
        movement_type IN (
            'RESTOCK',
            'SALE',
            'ADJUSTMENT',
            'TRANSFER_IN',
            'TRANSFER_OUT',
            'PURCHASE_RECEIPT',
            'RETURN_IN',
            'RETURN_OUT'
        )
    ),
    CONSTRAINT chk_admin_source_stock_movements_quantity_non_zero CHECK (quantity_delta <> 0),
    CONSTRAINT chk_admin_source_stock_movements_balance_non_negative CHECK (balance_after IS NULL OR balance_after >= 0)
);

CREATE INDEX IF NOT EXISTS idx_admin_source_stock_movements_source_created_at
    ON admin_source_stock_movements (source_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_admin_source_stock_movements_product_created_at
    ON admin_source_stock_movements (product_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_admin_source_stock_movements_type
    ON admin_source_stock_movements (movement_type);

CREATE TABLE IF NOT EXISTS admin_source_purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    source_id BIGINT NOT NULL REFERENCES sources(id) ON DELETE CASCADE,
    supplier_name VARCHAR(180) NOT NULL,
    supplier_reference VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    expected_at TIMESTAMP WITH TIME ZONE,
    placed_at TIMESTAMP WITH TIME ZONE,
    received_at TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    total_items INTEGER NOT NULL DEFAULT 0,
    total_quantity INTEGER NOT NULL DEFAULT 0,
    created_by_admin_user_id BIGINT REFERENCES admin_users(id) ON DELETE SET NULL,
    updated_by_admin_user_id BIGINT REFERENCES admin_users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_admin_source_purchase_orders_status CHECK (
        status IN ('DRAFT', 'SUBMITTED', 'PARTIALLY_RECEIVED', 'RECEIVED', 'CANCELED')
    ),
    CONSTRAINT chk_admin_source_purchase_orders_total_items_non_negative CHECK (total_items >= 0),
    CONSTRAINT chk_admin_source_purchase_orders_total_quantity_non_negative CHECK (total_quantity >= 0)
);

CREATE INDEX IF NOT EXISTS idx_admin_source_purchase_orders_source_status
    ON admin_source_purchase_orders (source_id, status);
CREATE INDEX IF NOT EXISTS idx_admin_source_purchase_orders_expected_at
    ON admin_source_purchase_orders (expected_at DESC);

CREATE TABLE IF NOT EXISTS admin_source_purchase_order_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL REFERENCES admin_source_purchase_orders(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id) ON DELETE SET NULL,
    sku VARCHAR(80),
    product_name VARCHAR(180) NOT NULL,
    ordered_qty INTEGER NOT NULL,
    received_qty INTEGER NOT NULL DEFAULT 0,
    unit_cost_minor BIGINT NOT NULL DEFAULT 0,
    currency_code CHAR(3) REFERENCES currencies(code),
    line_total_minor BIGINT NOT NULL DEFAULT 0,
    note TEXT,
    CONSTRAINT chk_admin_source_purchase_order_items_ordered_qty_positive CHECK (ordered_qty > 0),
    CONSTRAINT chk_admin_source_purchase_order_items_received_qty_non_negative CHECK (received_qty >= 0),
    CONSTRAINT chk_admin_source_purchase_order_items_unit_cost_non_negative CHECK (unit_cost_minor >= 0),
    CONSTRAINT chk_admin_source_purchase_order_items_line_total_non_negative CHECK (line_total_minor >= 0)
);

CREATE INDEX IF NOT EXISTS idx_admin_source_purchase_order_items_purchase_order
    ON admin_source_purchase_order_items (purchase_order_id);
CREATE INDEX IF NOT EXISTS idx_admin_source_purchase_order_items_product
    ON admin_source_purchase_order_items (product_id);

CREATE TABLE IF NOT EXISTS admin_source_transfers (
    id BIGSERIAL PRIMARY KEY,
    source_id_from BIGINT NOT NULL REFERENCES sources(id) ON DELETE CASCADE,
    source_id_to BIGINT NOT NULL REFERENCES sources(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    reference_code VARCHAR(80),
    requested_at TIMESTAMP WITH TIME ZONE,
    shipped_at TIMESTAMP WITH TIME ZONE,
    received_at TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    total_items INTEGER NOT NULL DEFAULT 0,
    total_quantity INTEGER NOT NULL DEFAULT 0,
    created_by_admin_user_id BIGINT REFERENCES admin_users(id) ON DELETE SET NULL,
    updated_by_admin_user_id BIGINT REFERENCES admin_users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_admin_source_transfers_status CHECK (
        status IN ('DRAFT', 'REQUESTED', 'IN_TRANSIT', 'COMPLETED', 'CANCELED')
    ),
    CONSTRAINT chk_admin_source_transfers_sources_different CHECK (source_id_from <> source_id_to),
    CONSTRAINT chk_admin_source_transfers_total_items_non_negative CHECK (total_items >= 0),
    CONSTRAINT chk_admin_source_transfers_total_quantity_non_negative CHECK (total_quantity >= 0)
);

CREATE INDEX IF NOT EXISTS idx_admin_source_transfers_source_from_status
    ON admin_source_transfers (source_id_from, status);
CREATE INDEX IF NOT EXISTS idx_admin_source_transfers_source_to_status
    ON admin_source_transfers (source_id_to, status);

CREATE TABLE IF NOT EXISTS admin_source_transfer_items (
    id BIGSERIAL PRIMARY KEY,
    transfer_id BIGINT NOT NULL REFERENCES admin_source_transfers(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id) ON DELETE SET NULL,
    sku VARCHAR(80),
    product_name VARCHAR(180) NOT NULL,
    quantity INTEGER NOT NULL,
    note TEXT,
    CONSTRAINT chk_admin_source_transfer_items_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX IF NOT EXISTS idx_admin_source_transfer_items_transfer
    ON admin_source_transfer_items (transfer_id);
CREATE INDEX IF NOT EXISTS idx_admin_source_transfer_items_product
    ON admin_source_transfer_items (product_id);

CREATE TABLE IF NOT EXISTS admin_source_alerts (
    id BIGSERIAL PRIMARY KEY,
    source_id BIGINT NOT NULL REFERENCES sources(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id) ON DELETE SET NULL,
    alert_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    threshold_value INTEGER,
    current_value INTEGER,
    message TEXT NOT NULL,
    triggered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by_admin_user_id BIGINT REFERENCES admin_users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_admin_source_alerts_type CHECK (
        alert_type IN ('LOW_STOCK', 'OUT_OF_STOCK', 'OVERSTOCK', 'STOCK_MISMATCH', 'TRANSFER_DELAY')
    ),
    CONSTRAINT chk_admin_source_alerts_status CHECK (
        status IN ('OPEN', 'ACKNOWLEDGED', 'RESOLVED', 'DISMISSED')
    ),
    CONSTRAINT chk_admin_source_alerts_severity CHECK (
        severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')
    )
);

CREATE INDEX IF NOT EXISTS idx_admin_source_alerts_source_status
    ON admin_source_alerts (source_id, status);
CREATE INDEX IF NOT EXISTS idx_admin_source_alerts_product_status
    ON admin_source_alerts (product_id, status);
CREATE INDEX IF NOT EXISTS idx_admin_source_alerts_triggered_at
    ON admin_source_alerts (triggered_at DESC);
