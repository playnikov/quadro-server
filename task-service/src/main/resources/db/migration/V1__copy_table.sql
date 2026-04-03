CREATE TABLE users_copy (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    avatar VARCHAR(500),
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMPTZ NOT NULL,
);

CREATE INDEX idx_users_copy_email ON users_copy(email);
CREATE INDEX idx_users_copy_role ON users_copy(role);
CREATE INDEX idx_users_copy_active ON users_copy(is_active);