ALTER TABLE projects
ADD COLUMN provider_code TEXT NOT NULL DEFAULT 'aws';

CREATE TABLE cloud_providers (
    id TEXT PRIMARY KEY,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE project_credentials (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    provider_id TEXT NOT NULL,
    credential_type TEXT NOT NULL,
    encrypted_payload TEXT NOT NULL,
    key_version TEXT NOT NULL,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    CONSTRAINT fk_project_credentials_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_credentials_provider FOREIGN KEY (provider_id) REFERENCES cloud_providers (id)
);

CREATE INDEX idx_projects_provider_code ON projects (provider_code);
CREATE INDEX idx_cloud_providers_code ON cloud_providers (code);
CREATE INDEX idx_project_credentials_project_id ON project_credentials (project_id);
