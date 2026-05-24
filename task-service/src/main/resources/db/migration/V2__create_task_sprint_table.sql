CREATE TYPE sprint_status AS ENUM ('PLANNED', 'ACTIVE', 'COMPLETED', 'CANCELLED');

CREATE TABLE task_sprint (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    goal TEXT,
    status sprint_status NOT NULL DEFAULT 'PLANNED',
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_sprint_dates CHECK (end_date > start_date),
    FOREIGN KEY (project_id) REFERENCES projects_copy(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users_copy(id) ON DELETE RESTRICT
);

CREATE INDEX idx_sprint_project ON task_sprint(project_id);
CREATE INDEX idx_sprint_name ON task_sprint(name);
CREATE INDEX idx_sprint_status ON task_sprint(status);
CREATE INDEX idx_sprint_dates ON task_sprint(start_date, end_date);