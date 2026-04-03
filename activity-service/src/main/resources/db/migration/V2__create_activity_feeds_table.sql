CREATE TABLE activity_feeds (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    activity_id UUID NOT NULL REFERENCES activities(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE(user_id, activity_id)
);

CREATE INDEX idx_activity_feeds_user ON activity_feeds(user_id, created_at DESC);