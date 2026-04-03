CREATE TABLE task_history (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    field VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_task_history_task ON task_history(task_id);
CREATE INDEX idx_task_history_user ON task_history(user_id);