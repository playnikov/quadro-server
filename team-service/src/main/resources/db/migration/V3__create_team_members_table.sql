CREATE TABLE team_members (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    joined_at TIMESTAMPTZ,
    invited_by UUID NOT NULL,
    invited_at TIMESTAMPTZ NOT NULL,
    last_active_at TIMESTAMPTZ,
    is_active BOOLEAN NOT NULL DEFAULT false,
    UNIQUE(team_id, user_id)
);

CREATE INDEX idx_team_members_team ON team_members(team_id);
CREATE INDEX idx_team_members_user ON team_members(user_id);