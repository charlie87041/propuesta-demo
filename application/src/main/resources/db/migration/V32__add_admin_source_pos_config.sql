CREATE TABLE IF NOT EXISTS admin_source_pos_configs (
    id BIGSERIAL PRIMARY KEY,
    source_id BIGINT NOT NULL UNIQUE REFERENCES sources(id) ON DELETE CASCADE,
    pos_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    default_currency_code CHAR(3) REFERENCES currencies(code),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_admin_source_pos_configs_pos_enabled
    ON admin_source_pos_configs (pos_enabled);
