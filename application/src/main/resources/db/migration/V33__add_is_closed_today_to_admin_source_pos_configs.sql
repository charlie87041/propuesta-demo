ALTER TABLE admin_source_pos_configs
    ADD COLUMN IF NOT EXISTS is_closed_today BOOLEAN NOT NULL DEFAULT TRUE;

