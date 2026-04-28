CREATE TABLE IF NOT EXISTS admin_source_purchase_order_status_history (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL REFERENCES admin_source_purchase_orders(id) ON DELETE CASCADE,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    changed_by_admin_user_id BIGINT REFERENCES admin_users(id) ON DELETE SET NULL,
    note TEXT,
    CONSTRAINT chk_admin_source_purchase_order_status_history_from_status CHECK (
        from_status IS NULL OR from_status IN ('DRAFT', 'SUBMITTED', 'PARTIALLY_RECEIVED', 'RECEIVED', 'CANCELED')
    ),
    CONSTRAINT chk_admin_source_purchase_order_status_history_to_status CHECK (
        to_status IN ('DRAFT', 'SUBMITTED', 'PARTIALLY_RECEIVED', 'RECEIVED', 'CANCELED')
    )
);

CREATE INDEX IF NOT EXISTS idx_admin_source_purchase_order_status_history_order_changed
    ON admin_source_purchase_order_status_history (purchase_order_id, changed_at DESC);

INSERT INTO admin_source_purchase_order_status_history (
    purchase_order_id,
    from_status,
    to_status,
    changed_at,
    changed_by_admin_user_id
)
SELECT
    po.id,
    NULL,
    po.status,
    COALESCE(po.received_at, po.placed_at, po.created_at, NOW()),
    COALESCE(po.updated_by_admin_user_id, po.created_by_admin_user_id)
FROM admin_source_purchase_orders po
WHERE NOT EXISTS (
    SELECT 1
    FROM admin_source_purchase_order_status_history h
    WHERE h.purchase_order_id = po.id
);
