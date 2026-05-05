CREATE TABLE users_copy (
    id UUID PRIMARY KEY,
    role VARCHAR (50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_users_copy_active ON users_copy(is_active);

CREATE TABLE projects_copy (
    id UUID PRIMARY KEY,
    key VARCHAR(10) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL
);

CREATE INDEX idx_projects_key ON projects_copy(key);

CREATE TABLE project_members_copy (
     id UUID PRIMARY KEY,
     project_id UUID NOT NULL,
     user_id UUID NOT NULL,
     role VARCHAR(50) NOT NULL,
     UNIQUE(project_id, user_id)
);

CREATE INDEX idx_project_members_company ON project_members_copy(project_id);
CREATE INDEX idx_project_members_user ON project_members_copy(user_id);

CREATE TABLE team_projects_copy (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL,
    project_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    UNIQUE(team_id, project_id)
);

CREATE INDEX idx_team_projects_team ON team_projects_copy(team_id);
CREATE INDEX idx_team_projects_project ON team_projects_copy(project_id);

CREATE TABLE teams_copy (
    id UUID PRIMARY KEY,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE team_members_copy (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT false,
    UNIQUE(team_id, user_id)
);

CREATE INDEX idx_team_members_team ON team_members_copy(team_id);
CREATE INDEX idx_team_members_user ON team_members_copy(user_id);