ALTER TABLE users
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

CREATE TABLE user_credentials (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_credentials_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);

INSERT INTO user_credentials (user_id, password_hash, email_verified)
SELECT id, password_hash, FALSE
FROM users
WHERE password_hash IS NOT NULL;

ALTER TABLE users
DROP COLUMN password_hash;

CREATE TABLE social_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(100) NOT NULL,
    provider_email VARCHAR(255),
    connected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_social_accounts_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT uk_social_accounts_provider_user
        UNIQUE (provider, provider_user_id),
    CONSTRAINT uk_social_accounts_user_provider
        UNIQUE (user_id, provider)
);

CREATE INDEX idx_social_accounts_user_id ON social_accounts (user_id);
