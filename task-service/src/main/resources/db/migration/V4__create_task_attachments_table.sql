CREATE TABLE task_attachments (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    uploaded_by UUID NOT NULL,
    file_name VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_task_attachments_task ON task_attachments(task_id);