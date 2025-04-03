CREATE TABLE IF NOT EXISTS idempotency_keys (
    id bigint primary key generated always as identity,
    key uuid not null unique,
    issued_at timestamp with time zone not null default CURRENT_TIMESTAMP,
    response_data text not null
)