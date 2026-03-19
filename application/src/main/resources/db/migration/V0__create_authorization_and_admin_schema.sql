CREATE TABLE IF NOT EXISTS domains (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS abilities (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS ability_permissions (
    ability_id BIGINT NOT NULL REFERENCES abilities(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (ability_id, permission_id)
);

CREATE TABLE IF NOT EXISTS admin_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS user_domain_abilities (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    domain_id BIGINT NOT NULL REFERENCES domains(id) ON DELETE CASCADE,
    ability_id BIGINT NOT NULL REFERENCES abilities(id) ON DELETE CASCADE,
    granted BOOLEAN NOT NULL DEFAULT TRUE,
    granted_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_user_domain_ability UNIQUE (user_id, domain_id, ability_id)
);

CREATE INDEX IF NOT EXISTS idx_uda_user_id ON user_domain_abilities (user_id);
CREATE INDEX IF NOT EXISTS idx_uda_domain_id ON user_domain_abilities (domain_id);
CREATE INDEX IF NOT EXISTS idx_uda_lookup ON user_domain_abilities (user_id, domain_id, granted);

CREATE TABLE IF NOT EXISTS user_domain_permission_overrides (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    domain_id BIGINT NOT NULL REFERENCES domains(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    granted BOOLEAN NOT NULL,
    granted_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_user_domain_permission UNIQUE (user_id, domain_id, permission_id)
);

CREATE INDEX IF NOT EXISTS idx_udpo_lookup ON user_domain_permission_overrides (user_id, domain_id);
CREATE INDEX IF NOT EXISTS idx_admin_users_email ON admin_users (email);
