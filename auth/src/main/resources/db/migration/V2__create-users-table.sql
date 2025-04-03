CREATE TABLE IF NOT EXISTS users (
    uuid uuid primary key default gen_random_uuid(),
    email varchar(255) not null unique,
    password varchar(255) not null,
    registered_at timestamp with time zone not null default CURRENT_TIMESTAMP,
    is_email_verified bool not null default false,
    deactivated_at timestamp with time zone
);
