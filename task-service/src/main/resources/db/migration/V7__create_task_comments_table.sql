CREATE TABLE task_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL,
    author_id UUID NOT NULL,
    content TEXT NOT NULL,
    parent_id UUID REFERENCES task_comments(id) ON DELETE CASCADE,
    is_edited BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    mentions UUID[],
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users_copy(id) ON DELETE CASCADE
);

CREATE INDEX idx_task_comments_task ON task_comments(task_id);
CREATE INDEX idx_task_comments_author ON task_comments(author_id);
CREATE INDEX idx_task_comments_parent ON task_comments(parent_id);
CREATE INDEX idx_task_comments_created_at ON task_comments(created_at);
CREATE INDEX idx_task_comments_mentions ON task_comments USING GIN (mentions);