CREATE TABLE users_copy (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NUll,
    is_active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_users_copy_active ON users_copy(is_active);
CREATE INDEX idx_users_copy_email ON users_copy(email);