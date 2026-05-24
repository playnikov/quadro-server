CREATE TYPE invite_status AS ENUM ('PENDING', 'ACCEPTED', 'EXPIRED', 'CANCELLED');
CREATE TYPE invite_type AS ENUM ('EMAIL', 'LINK');

CREATE TABLE project_invitations (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
     invited_by UUID NOT NULL,
     type invite_type NOT NULL DEFAULT 'EMAIL',
     identifier VARCHAR(255) NOT NULL,
     role member_roles NOT NULL DEFAULT 'MEMBER',
     status invite_status NOT NULL DEFAULT 'PENDING',
     token VARCHAR(500) NOT NULL UNIQUE,
     expires_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() + INTERVAL '7 days'),
     created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
     accepted_at TIMESTAMP WITH TIME ZONE,
     accepted_by UUID,
     message TEXT
);

CREATE INDEX idx_project_invitations_project ON project_invitations(project_id);
CREATE INDEX idx_project_invitations_status ON project_invitations(status);
CREATE INDEX idx_project_invitations_token ON project_invitations(token);

--SELECT cron.schedule(
--  'expire-invitations',
--  '* * * * *',
--  $$UPDATE project_schema.project_invitations
--     SET status = 'EXPIRED'::invite_status
--     WHERE status = 'PENDING'::invite_status AND expires_at < NOW()$$
--);