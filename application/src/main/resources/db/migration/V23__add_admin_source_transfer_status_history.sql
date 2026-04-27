CREATE TABLE IF NOT EXISTS admin_source_transfer_status_history (
    id BIGSERIAL PRIMARY KEY,
    transfer_id BIGINT NOT NULL REFERENCES admin_source_transfers(id) ON DELETE CASCADE,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    changed_by_admin_user_id BIGINT REFERENCES admin_users(id) ON DELETE SET NULL,
    note TEXT,
    CONSTRAINT chk_admin_source_transfer_status_history_from_status CHECK (
        from_status IS NULL OR from_status IN ('DRAFT', 'REQUESTED', 'IN_TRANSIT', 'COMPLETED', 'CANCELED')
    ),
    CONSTRAINT chk_admin_source_transfer_status_history_to_status CHECK (
        to_status IN ('DRAFT', 'REQUESTED', 'IN_TRANSIT', 'COMPLETED', 'CANCELED')
    )
);

CREATE INDEX IF NOT EXISTS idx_admin_source_transfer_status_history_transfer_changed
    ON admin_source_transfer_status_history (transfer_id, changed_at DESC);

INSERT INTO admin_source_transfer_status_history (
    transfer_id,
    from_status,
    to_status,
    changed_at,
    changed_by_admin_user_id
)
SELECT
    t.id,
    NULL,
    t.status,
    COALESCE(t.received_at, t.shipped_at, t.requested_at, t.created_at, NOW()),
    COALESCE(t.updated_by_admin_user_id, t.created_by_admin_user_id)
FROM admin_source_transfers t
WHERE NOT EXISTS (
    SELECT 1
    FROM admin_source_transfer_status_history h
    WHERE h.transfer_id = t.id
);
