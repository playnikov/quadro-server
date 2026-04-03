CREATE TABLE project_teams (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES project.projects(id) ON DELETE CASCADE,
    team_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_lead_team BOOLEAN NOT NULL DEFAULT false,
    assigned_at TIMESTAMPTZ NOT NULL,
    assigned_by UUID NOT NULL,
    UNIQUE(project_id, team_id)
);

CREATE INDEX idx_project_teams_project ON project_teams(project_id);