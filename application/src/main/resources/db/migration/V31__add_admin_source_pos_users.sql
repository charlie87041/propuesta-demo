CREATE TABLE IF NOT EXISTS admin_source_pos_users (
    id BIGSERIAL PRIMARY KEY,
    source_id BIGINT NOT NULL REFERENCES sources(id) ON DELETE CASCADE,
    admin_user_id BIGINT NOT NULL REFERENCES admin_users(id) ON DELETE CASCADE,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    assigned_by_admin_user_id BIGINT REFERENCES admin_users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_admin_source_pos_users_source_user UNIQUE (source_id, admin_user_id),
    CONSTRAINT chk_admin_source_pos_users_role CHECK (role IN ('POS_CASHIER', 'POS_SUPERVISOR', 'POS_MANAGER'))
);

CREATE INDEX IF NOT EXISTS idx_admin_source_pos_users_source_active
    ON admin_source_pos_users (source_id, active);
