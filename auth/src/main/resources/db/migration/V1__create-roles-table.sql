CREATE TABLE IF NOT EXISTS roles (
    id bigint primary key generated always as identity,
    role_name varchar(255) not null unique
);