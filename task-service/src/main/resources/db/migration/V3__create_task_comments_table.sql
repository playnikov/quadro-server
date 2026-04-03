CREATE TABLE task_comments (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    author_id UUID NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'COMMENT',
    attachments TEXT,
    mentions TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    edited BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_task_comments_task ON task_comments(task_id);
CREATE INDEX idx_task_comments_author ON task_comments(author_id);