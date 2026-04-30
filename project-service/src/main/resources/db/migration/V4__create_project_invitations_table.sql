CREATE TABLE project_invitations (
     id UUID PRIMARY KEY,
     project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
     invited_by UUID NOT NULL,
     invite_type VARCHAR(50) NOT NULL,
     identifier VARCHAR(255) NOT NULL,
     role VARCHAR(50) NOT NULL,
     status VARCHAR(50) NOT NULL,
     token VARCHAR(500) NOT NULL UNIQUE,
     expires_at TIMESTAMPTZ NOT NULL,
     created_at TIMESTAMPTZ NOT NULL,
     accepted_at TIMESTAMPTZ,
     accepted_by UUID,
     message TEXT
);

CREATE INDEX idx_project_invitations_project ON project_invitations(project_id);
CREATE INDEX idx_project_invitations_token ON project_invitations(token);