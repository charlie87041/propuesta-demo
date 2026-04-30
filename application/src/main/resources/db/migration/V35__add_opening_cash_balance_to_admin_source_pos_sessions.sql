ALTER TABLE admin_source_pos_sessions
    ADD COLUMN IF NOT EXISTS opening_cash_balance NUMERIC(12,2) NOT NULL DEFAULT 0;

