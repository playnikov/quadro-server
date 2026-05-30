CREATE TABLE project_task_sequence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID REFERENCES projects_copy(id) ON DELETE CASCADE,
    last_number INTEGER NOT NULL DEFAULT 0
);