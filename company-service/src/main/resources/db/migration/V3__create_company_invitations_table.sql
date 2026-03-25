CREATE TABLE company_invitations (
     id UUID PRIMARY KEY,
     company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
     team_id UUID,
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

CREATE INDEX idx_company_invitations_company ON company_invitations(company_id);
CREATE INDEX idx_company_invitations_token ON company_invitations(token);