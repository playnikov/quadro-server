CREATE TABLE users_copy (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    avatar VARCHAR(500),
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMPTZ NOT NULL,
);

CREATE INDEX idx_users_copy_email ON users_copy(email);
CREATE INDEX idx_users_copy_role ON users_copy(role);
CREATE INDEX idx_users_copy_active ON users_copy(is_active);

CREATE TABLE companies_copy (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_companies_copy_status ON companies_copy(status);

CREATE TABLE projects_copy (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    key VARCHAR(10) NOT NULL,
    status VARCHAR(50) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_projects_copy_company ON projects_copy(company_id);