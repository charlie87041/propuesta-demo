ALTER TABLE admin_source_pos_configs
    ADD COLUMN IF NOT EXISTS force_cash_breakdown_on_close BOOLEAN NOT NULL DEFAULT FALSE;
