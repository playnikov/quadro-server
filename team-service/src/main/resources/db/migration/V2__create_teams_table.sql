CREATE TABLE teams (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    avatar VARCHAR(500),
    status VARCHAR(50) NOT NULL,
    visibility VARCHAR(50) NOT NULL,
    lead_id UUID NOT NULL,
    settings TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    archived_at TIMESTAMPTZ,
    max_members INT NOT NULL,
    current_members INT NOT NULL,
    UNIQUE(company_id, name)
);

CREATE INDEX idx_teams_company ON teams(company_id);
CREATE INDEX idx_teams_lead ON teams(lead_id);