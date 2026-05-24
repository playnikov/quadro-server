CREATE TYPE team_status AS ENUM ('ACTIVE', 'ARCHIVED', 'DELETED');

CREATE TABLE teams (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    avatar VARCHAR(500),
    status team_status NOT NULL DEFAULT 'ACTIVE',
    visibility VARCHAR(50) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_teams_name ON teams(name);
CREATE INDEX idx_teams_status ON teams(status);