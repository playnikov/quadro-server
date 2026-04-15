CREATE TABLE users_copy (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    avatar VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_users_copy_email ON users_copy(email);
CREATE INDEX idx_users_copy_active ON users_copy(is_active);

CREATE TABLE companies_copy (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    create_role VARCHAR (50) NOT NULL,
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
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_projects_copy_company ON projects_copy(company_id);