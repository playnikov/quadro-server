CREATE TYPE notification_level AS ENUM ('ALL', 'MENTIONS_ONLY', 'NONE');

CREATE TABLE task_watchers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL,
    user_id UUID NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    notification_level notification_level NOT NULL DEFAULT 'ALL',
    UNIQUE(task_id, user_id),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users_copy(id) ON DELETE CASCADE
);

CREATE INDEX idx_task_watchers_task ON task_watchers(task_id);
CREATE INDEX idx_task_watchers_user ON task_watchers(user_id);
CREATE INDEX idx_task_watchers_user_level ON task_watchers(user_id, notification_level);