CREATE TABLE company_members (
     id UUID PRIMARY KEY,
     company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
     user_id UUID NOT NULL,
     role VARCHAR(50) NOT NULL,
     joined_at TIMESTAMPTZ NOT NULL,
     invited_by UUID NOT NULL,
     invited_at TIMESTAMPTZ NOT NULL,
     last_active_at TIMESTAMPTZ,
     is_active BOOLEAN NOT NULL DEFAULT true,
     UNIQUE(company_id, user_id)
);

CREATE INDEX idx_company_members_company ON company_members(company_id);
CREATE INDEX idx_company_members_user ON company_members(user_id);