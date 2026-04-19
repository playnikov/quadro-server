CREATE TABLE users_copy (
    id UUID PRIMARY KEY,
    is_active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_users_copy_active ON users_copy(is_active);