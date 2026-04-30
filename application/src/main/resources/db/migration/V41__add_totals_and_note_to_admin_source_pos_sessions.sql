ALTER TABLE admin_source_pos_sessions
    ADD COLUMN cash_payments_total_minor BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN other_payments_total_minor BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN total_sales_minor BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN drawer_note VARCHAR(500);

