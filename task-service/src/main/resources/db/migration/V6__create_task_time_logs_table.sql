CREATE TABLE task_time_logs (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    time_spent TIMESTAMPTZ NOT NULL,
    description TEXT,
    logged_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_task_time_logs_task ON task_time_logs(task_id);
CREATE INDEX idx_task_time_logs_user ON task_time_logs(user_id);