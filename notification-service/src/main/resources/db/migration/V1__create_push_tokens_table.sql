CREATE TABLE push_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    platform VARCHAR(50) NOT NULL,
    device_id VARCHAR(255),
    device_name VARCHAR(255),
    app_version VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    last_used_at TIMESTAMPTZ
);

CREATE INDEX idx_push_tokens_user ON push_tokens(user_id);
CREATE INDEX idx_push_tokens_status ON push_tokens(status);