ALTER TABLE admin_source_pos_orders
    ADD COLUMN pos_session_id BIGINT NULL;

ALTER TABLE admin_source_pos_orders
    ADD CONSTRAINT fk_admin_source_pos_orders_pos_session
        FOREIGN KEY (pos_session_id) REFERENCES admin_source_pos_sessions(id);

CREATE INDEX idx_admin_source_pos_orders_pos_session_id
    ON admin_source_pos_orders(pos_session_id);

