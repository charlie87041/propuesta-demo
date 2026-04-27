ALTER TABLE admin_source_transfer_incident_items
    ADD COLUMN IF NOT EXISTS archived BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS closed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS closed_by_admin_user_id BIGINT REFERENCES admin_users(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS reverted_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS reverted_by_admin_user_id BIGINT REFERENCES admin_users(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_admin_source_transfer_incident_items_archived
    ON admin_source_transfer_incident_items (archived);
