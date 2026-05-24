CREATE TYPE history_action AS ENUM (
    'CREATE', 'UPDATE', 'STATUS_CHANGE', 'ASSIGNEE_CHANGE',
    'SPRINT_CHANGE', 'PRIORITY_CHANGE', 'TYPE_CHANGE',
    'COMMENT_ADDED', 'ATTACHMENT_ADDED', 'DELETE'
);

CREATE TABLE task_history (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    user_id UUID NOT NULL,
    action history_action NOT NULL,
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users_copy(id) ON DELETE CASCADE
);

CREATE INDEX idx_task_history_task ON task_history(task_id);
CREATE INDEX idx_task_history_user ON task_history(user_id);
CREATE INDEX idx_task_history_task_action_time ON task_history(task_id, action, created_at);