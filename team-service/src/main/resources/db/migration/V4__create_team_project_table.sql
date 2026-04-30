CREATE TABLE team_projects (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    project_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    bound_at TIMESTAMPTZ NOT NULL,
    bound_by UUID NOT NULL,
    UNIQUE(team_id, project_id)
);

CREATE INDEX idx_team_projects_team ON team_projects(team_id);
CREATE INDEX idx_team_projects_project ON team_projects(project_id);