CREATE TYPE user_roles AS ENUM ('SUPER_ADMIN', 'ADMIN', 'USER');

CREATE TABLE users_copy (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    last_name VARCHAR (100),
    first_name VARCHAR (100),
    middle_name VARCHAR (100),
    role user_roles NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_users_copy_active ON users_copy(is_active);
CREATE INDEX idx_users_copy_email ON users_copy(email);