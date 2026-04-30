CREATE TABLE IF NOT EXISTS admin_source_pos_sessions (
    id BIGSERIAL PRIMARY KEY,
    source_id BIGINT NOT NULL REFERENCES sources(id) ON DELETE CASCADE,
    session_date DATE NOT NULL,
    opened_by_admin_user_id BIGINT REFERENCES admin_users(id) ON DELETE SET NULL,
    closed_by_admin_user_id BIGINT REFERENCES admin_users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_admin_source_pos_sessions_source_date UNIQUE (source_id, session_date)
);

CREATE INDEX IF NOT EXISTS idx_admin_source_pos_sessions_source_date
    ON admin_source_pos_sessions (source_id, session_date DESC);

