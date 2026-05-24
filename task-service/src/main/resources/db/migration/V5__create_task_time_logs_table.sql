CREATE TABLE task_time_logs (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    user_id UUID NOT NULL,
    time_spent NUMERIC(10,2) NOT NULL,
    description TEXT,
    logged_at TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users_copy(id) ON DELETE CASCADE
);

CREATE INDEX idx_task_time_logs_task ON task_time_logs(task_id);
CREATE INDEX idx_task_time_logs_user ON task_time_logs(user_id);
CREATE INDEX idx_task_time_logs_logged_at ON task_time_logs(logged_at);