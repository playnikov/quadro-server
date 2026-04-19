CREATE TABLE projects (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    key VARCHAR(10) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    visibility VARCHAR(50) NOT NULL,
    lead_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    start_date TIMESTAMPTZ,
    end_date TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE(company_id, key),
    UNIQUE(company_id, name)
);

CREATE INDEX idx_projects_company ON projects(company_id);
CREATE INDEX idx_projects_lead ON projects(lead_id);