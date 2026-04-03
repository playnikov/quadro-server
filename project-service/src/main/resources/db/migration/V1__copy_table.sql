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

CREATE INDEX idx_users_copy_email ON users(email);
CREATE INDEX idx_users_copy_role ON users(role);
CREATE INDEX idx_users_copy_active ON users(is_active);

CREATE TABLE companies_copy (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    current_projects INT NOT NULL,
    max_projects INT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_companies_copy_status ON companies_copy(status);

CREATE TABLE teams_copy (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_teams_copy_company ON teams_copy(company_id);