CREATE TYPE member_roles AS ENUM ('OWNER', 'MANAGER', 'MEMBER', 'GUEST');

CREATE TABLE project_members (
     id UUID PRIMARY KEY,
     project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
     user_id UUID NOT NULL,
     role member_roles NOT NULL DEFAULT 'MEMBER',
     joined_at TIMESTAMP WITH TIME ZONE NOT NULL,
     invited_by UUID NOT NULL,
     invited_at TIMESTAMP WITH TIME ZONE NOT NULL,
     UNIQUE(project_id, user_id)
);

CREATE INDEX idx_project_members ON project_members(project_id);
CREATE INDEX idx_project_members_user ON project_members(user_id);