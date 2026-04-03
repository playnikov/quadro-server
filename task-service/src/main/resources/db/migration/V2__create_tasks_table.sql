CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    parent_id UUID REFERENCES tasks(id) ON DELETE CASCADE,
    key VARCHAR(20) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    resolution VARCHAR(50),
    assignee_id UUID,
    reporter_id UUID NOT NULL,
    story_points INT,
    time_estimate TIMESTAMPTZ,
    time_spent TIMESTAMPTZ NOT NULL DEFAULT 0,
    due_date TIMESTAMPTZ,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    "order" INT NOT NULL,
    tags TEXT,
    UNIQUE(project_id, key)
);

CREATE INDEX idx_tasks_project ON tasks(project_id);
CREATE INDEX idx_tasks_assignee ON tasks(assignee_id);
CREATE INDEX idx_tasks_reporter ON tasks(reporter_id);
CREATE INDEX idx_tasks_key ON tasks(key);