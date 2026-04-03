CREATE TABLE team_projects (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL REFERENCES team.teams(id) ON DELETE CASCADE,
    project_id UUID NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL,
    assigned_by UUID NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    UNIQUE(team_id, project_id)
);

CREATE INDEX idx_team_projects_team ON team_projects(team_id);
CREATE INDEX idx_team_projects_project ON team_projects(project_id);