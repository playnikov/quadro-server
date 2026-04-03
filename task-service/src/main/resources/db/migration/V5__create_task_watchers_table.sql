CREATE TABLE task_watchers (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    added_at TIMESTAMPTZ NOT NULL,
    notification_level VARCHAR(50) NOT NULL DEFAULT 'ALL',
    UNIQUE(task_id, user_id)
);

CREATE INDEX idx_task_watchers_task ON task_watchers(task_id);
CREATE INDEX idx_task_watchers_user ON task_watchers(user_id);