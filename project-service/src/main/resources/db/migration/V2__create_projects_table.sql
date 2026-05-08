CREATE TABLE projects (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    key VARCHAR(10) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    visibility VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_projects_type ON projects(type);
CREATE INDEX idx_projects_name ON projects(name);
CREATE INDEX idx_projects_key ON projects(key);