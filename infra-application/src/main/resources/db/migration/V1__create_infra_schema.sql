CREATE TABLE projects (
    id TEXT PRIMARY KEY,
    project_key TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    default_region TEXT NOT NULL,
    owner TEXT,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE environments (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    environment_name TEXT NOT NULL,
    region TEXT,
    domain TEXT,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    CONSTRAINT fk_environments_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT uq_environments_project_name UNIQUE (project_id, environment_name)
);

CREATE TABLE project_applications (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    application_key TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    build_type TEXT NOT NULL,
    service_type TEXT NOT NULL,
    runtime TEXT,
    source_location TEXT,
    default_port INTEGER,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    CONSTRAINT fk_project_applications_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT uq_project_applications_project_key UNIQUE (project_id, application_key)
);

CREATE TABLE project_resource_definitions (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    environment_id TEXT NOT NULL,
    resource_type TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    config_json TEXT NOT NULL,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    CONSTRAINT fk_project_resource_definitions_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_resource_definitions_environment FOREIGN KEY (environment_id) REFERENCES environments (id)
);

CREATE TABLE application_service_definitions (
    id TEXT PRIMARY KEY,
    application_id TEXT NOT NULL,
    environment_id TEXT NOT NULL,
    service_type TEXT NOT NULL,
    name TEXT NOT NULL,
    config_json TEXT NOT NULL,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    CONSTRAINT fk_application_service_definitions_application FOREIGN KEY (application_id) REFERENCES project_applications (id),
    CONSTRAINT fk_application_service_definitions_environment FOREIGN KEY (environment_id) REFERENCES environments (id),
    CONSTRAINT uq_application_service_definitions_app_env UNIQUE (application_id, environment_id)
);

CREATE TABLE application_resource_bindings (
    id TEXT PRIMARY KEY,
    application_id TEXT NOT NULL,
    environment_id TEXT NOT NULL,
    project_resource_definition_id TEXT NOT NULL,
    binding_type TEXT NOT NULL,
    mount_as TEXT,
    config_json TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    CONSTRAINT fk_application_resource_bindings_application FOREIGN KEY (application_id) REFERENCES project_applications (id),
    CONSTRAINT fk_application_resource_bindings_environment FOREIGN KEY (environment_id) REFERENCES environments (id),
    CONSTRAINT fk_application_resource_bindings_definition FOREIGN KEY (project_resource_definition_id) REFERENCES project_resource_definitions (id)
);

CREATE TABLE deployment_definitions (
    id TEXT PRIMARY KEY,
    application_id TEXT NOT NULL,
    environment_id TEXT NOT NULL,
    build_type TEXT NOT NULL,
    artifact_source TEXT,
    entrypoint TEXT,
    deploy_config_json TEXT NOT NULL,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    CONSTRAINT fk_deployment_definitions_application FOREIGN KEY (application_id) REFERENCES project_applications (id),
    CONSTRAINT fk_deployment_definitions_environment FOREIGN KEY (environment_id) REFERENCES environments (id),
    CONSTRAINT uq_deployment_definitions_app_env UNIQUE (application_id, environment_id)
);

CREATE TABLE deployment_runs (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    environment_id TEXT NOT NULL,
    application_id TEXT,
    operation TEXT NOT NULL,
    status TEXT NOT NULL,
    triggered_by TEXT,
    started_at TEXT,
    finished_at TEXT,
    summary TEXT,
    logs_path TEXT,
    error_message TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    CONSTRAINT fk_deployment_runs_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_deployment_runs_environment FOREIGN KEY (environment_id) REFERENCES environments (id),
    CONSTRAINT fk_deployment_runs_application FOREIGN KEY (application_id) REFERENCES project_applications (id)
);

CREATE TABLE resource_outputs (
    id TEXT PRIMARY KEY,
    deployment_run_id TEXT NOT NULL,
    resource_type TEXT NOT NULL,
    resource_name TEXT NOT NULL,
    output_key TEXT NOT NULL,
    output_value TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    CONSTRAINT fk_resource_outputs_run FOREIGN KEY (deployment_run_id) REFERENCES deployment_runs (id)
);

CREATE TABLE infra_users (
    id TEXT PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    display_name TEXT NOT NULL,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE INDEX idx_environments_project_id ON environments (project_id);
CREATE INDEX idx_project_applications_project_id ON project_applications (project_id);
CREATE INDEX idx_project_resource_definitions_environment_id ON project_resource_definitions (environment_id);
CREATE INDEX idx_application_service_definitions_application_id ON application_service_definitions (application_id);
CREATE INDEX idx_application_resource_bindings_application_environment ON application_resource_bindings (application_id, environment_id);
CREATE INDEX idx_deployment_runs_project_id ON deployment_runs (project_id);
CREATE INDEX idx_resource_outputs_run_id ON resource_outputs (deployment_run_id);
