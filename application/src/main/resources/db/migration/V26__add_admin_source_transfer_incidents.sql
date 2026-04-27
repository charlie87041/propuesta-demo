CREATE TABLE IF NOT EXISTS admin_source_transfer_incidents (
    id BIGSERIAL PRIMARY KEY,
    transfer_id BIGINT NOT NULL REFERENCES admin_source_transfers(id) ON DELETE CASCADE,
    incident_type VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    created_by_admin_user_id BIGINT REFERENCES admin_users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_admin_source_transfer_incidents_type CHECK (
        incident_type IN ('COMPLETE', 'CANCEL')
    )
);

CREATE INDEX IF NOT EXISTS idx_admin_source_transfer_incidents_transfer_created
    ON admin_source_transfer_incidents (transfer_id, created_at DESC);

CREATE TABLE IF NOT EXISTS admin_source_transfer_incident_items (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL REFERENCES admin_source_transfer_incidents(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id) ON DELETE SET NULL,
    product_name VARCHAR(180) NOT NULL,
    expected_quantity INTEGER NOT NULL,
    missing_quantity INTEGER NOT NULL,
    received_quantity INTEGER NOT NULL,
    CONSTRAINT chk_admin_source_transfer_incident_items_expected_non_negative CHECK (expected_quantity >= 0),
    CONSTRAINT chk_admin_source_transfer_incident_items_missing_non_negative CHECK (missing_quantity >= 0),
    CONSTRAINT chk_admin_source_transfer_incident_items_received_non_negative CHECK (received_quantity >= 0),
    CONSTRAINT chk_admin_source_transfer_incident_items_missing_not_gt_expected CHECK (missing_quantity <= expected_quantity),
    CONSTRAINT chk_admin_source_transfer_incident_items_received_consistent CHECK (
        received_quantity = expected_quantity - missing_quantity
    )
);

CREATE INDEX IF NOT EXISTS idx_admin_source_transfer_incident_items_incident
    ON admin_source_transfer_incident_items (incident_id);

CREATE INDEX IF NOT EXISTS idx_admin_source_transfer_incident_items_product
    ON admin_source_transfer_incident_items (product_id);
