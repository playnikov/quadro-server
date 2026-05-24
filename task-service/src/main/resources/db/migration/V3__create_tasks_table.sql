CREATE TYPE task_status AS ENUM ('BACKLOG', 'TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'CANCELLED');
CREATE TYPE task_priority AS ENUM ('LOWEST', 'LOW', 'MEDIUM', 'HIGH', 'HIGHEST');
CREATE TYPE task_type AS ENUM ('TASK', 'BUG', 'STORY', 'EPIC', 'SUBTASK');

CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    sprint_id UUID REFERENCES task_sprint(id) ON DELETE SET NULL,
    parent_task_id UUID REFERENCES tasks(id) ON DELETE CASCADE,
    number INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status task_status NOT NULL DEFAULT 'TODO',
    priority task_priority NOT NULL DEFAULT 'MEDIUM',
    type task_type NOT NULL DEFAULT 'TASK',
    assignee_id UUID,
    reporter_id UUID NOT NULL,
    story_points INTEGER,
    estimated_hours DOUBLE PRECISION,
    logged_hours DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    due_date TIMESTAMP WITH TIME ZONE,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    labels TEXT[],
    CONSTRAINT unique_project_number UNIQUE (project_id, number),
    CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects_copy(id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_assignee FOREIGN KEY (assignee_id) REFERENCES users_copy(id) ON DELETE SET NULL,
    CONSTRAINT fk_tasks_reporter FOREIGN KEY (reporter_id) REFERENCES users_copy(id) ON DELETE RESTRICT,
    CONSTRAINT chk_story_points CHECK (story_points IS NULL OR story_points > 0),
    CONSTRAINT chk_estimated_hours CHECK (estimated_hours IS NULL OR estimated_hours >= 0),
    CONSTRAINT chk_logged_hours CHECK (logged_hours >= 0)
);

CREATE INDEX idx_tasks_project ON tasks(project_id);
CREATE INDEX idx_tasks_sprint ON tasks(sprint_id);
CREATE INDEX idx_tasks_assignee ON tasks(assignee_id);
CREATE INDEX idx_tasks_reporter ON tasks(reporter_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_type ON tasks(type);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);