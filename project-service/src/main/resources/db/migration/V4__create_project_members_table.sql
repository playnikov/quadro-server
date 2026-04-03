CREATE TABLE project_members (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES project.projects(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,
    invited_by UUID NOT NULL,
    invited_at TIMESTAMPTZ NOT NULL,
    last_active_at TIMESTAMPTZ,
    source_team_id UUID,
    UNIQUE(project_id, user_id)
);

CREATE INDEX idx_project_members_project ON project_members(project_id);
CREATE INDEX idx_project_members_user ON project_members(user_id);