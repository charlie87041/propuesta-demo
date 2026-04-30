CREATE TABLE admin_source_pos_session_denominations (
    id BIGSERIAL PRIMARY KEY,
    pos_session_id BIGINT NOT NULL REFERENCES admin_source_pos_sessions(id) ON DELETE CASCADE,
    denomination_id BIGINT NOT NULL REFERENCES admin_source_currency_denominations(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_admin_source_pos_session_denomination UNIQUE (pos_session_id, denomination_id)
);

CREATE INDEX idx_admin_source_pos_session_denominations_session
    ON admin_source_pos_session_denominations(pos_session_id);

CREATE INDEX idx_admin_source_pos_session_denominations_denomination
    ON admin_source_pos_session_denominations(denomination_id);

