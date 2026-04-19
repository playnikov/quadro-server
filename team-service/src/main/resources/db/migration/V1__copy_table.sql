CREATE TABLE users_copy (
    id UUID PRIMARY KEY,
    is_active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_users_copy_active ON users_copy(is_active);

CREATE TABLE companies_copy (
    id UUID PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    manage_team VARCHAR (50) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_companies_copy_status ON companies_copy(status);

CREATE TABLE company_members_copy (
     id UUID PRIMARY KEY,
     company_id UUID NOT NULL,
     user_id UUID NOT NULL,
     role VARCHAR(50) NOT NULL,
     UNIQUE(company_id, user_id)
);

CREATE INDEX idx_company_members_company ON company_members_copy(company_id);
CREATE INDEX idx_company_members_user ON company_members_copy(user_id);

CREATE TABLE projects_copy (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_projects_copy_company ON projects_copy(company_id);