CREATE TABLE activities (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    user_avatar VARCHAR(500),
    type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    entity_name VARCHAR(500) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT NOT NULL,
    metadata TEXT,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_activities_user ON activities(user_id, created_at DESC);
CREATE INDEX idx_activities_entity ON activities(entity_type, entity_id, created_at DESC);
CREATE INDEX idx_activities_created ON activities(created_at DESC);