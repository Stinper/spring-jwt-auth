CREATE TABLE IF NOT EXISTS refresh_tokens (
    id bigint primary key generated always as identity,
    user_id uuid not null,
    token text not null unique,
    created_at timestamp with time zone not null default CURRENT_TIMESTAMP,
    expires_at timestamp with time zone not null,

    CONSTRAINT fk_refresh_tokens_user_id FOREIGN KEY (user_id) REFERENCES users(uuid)
)