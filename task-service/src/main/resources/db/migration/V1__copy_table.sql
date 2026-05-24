CREATE TYPE user_roles AS ENUM ('SUPER_ADMIN', 'ADMIN', 'USER');

CREATE TABLE users_copy (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    last_name VARCHAR (100),
    first_name VARCHAR (100),
    middle_name VARCHAR (100),
    role user_roles NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_users_copy_active ON users_copy(is_active);
CREATE INDEX idx_users_copy_email ON users_copy(email);

CREATE TYPE project_status AS ENUM ('ACTIVE', 'ON_HOLD', 'COMPLETED', 'ARCHIVED', 'CANCELLED');
CREATE TABLE projects_copy (
    id UUID PRIMARY KEY,
    key VARCHAR(10) NOT NULL UNIQUE,
    status project_status
);

CREATE INDEX idx_projects_key ON projects_copy(key);
CREATE INDEX idx_projects_status ON projects_copy(status);

CREATE TYPE member_roles AS ENUM ('OWNER', 'MANAGER', 'MEMBER', 'GUEST');

CREATE TABLE project_members_copy (
     id UUID PRIMARY KEY,
     project_id UUID NOT NULL,
     user_id UUID NOT NULL,
     role member_roles NOT NULL DEFAULT 'MEMBER',
     UNIQUE(project_id, user_id),
     FOREIGN KEY (project_id) REFERENCES projects_copy(id) ON DELETE CASCADE,
     FOREIGN KEY (user_id) REFERENCES users_copy(id) ON DELETE CASCADE
);

CREATE INDEX idx_project_members_company ON project_members_copy(project_id);
CREATE INDEX idx_project_members_user ON project_members_copy(user_id);