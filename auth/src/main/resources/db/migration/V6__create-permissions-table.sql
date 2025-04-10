CREATE TABLE IF NOT EXISTS permissions(
    id bigint primary key generated always as identity,
    permission varchar(255) not null unique,
    description text
);


ALTER TABLE roles ADD COLUMN prefix varchar(255) not null default '';

CREATE TABLE IF NOT EXISTS roles_permissions(
    id bigint primary key generated always as identity,
    role_id bigint not null,
    permission_id bigint not null,

    -- Композитный ключ, который не дает присвоить одной роли одно и то же право доступа более одного раза
    CONSTRAINT ck_role_id_permission_id UNIQUE (role_id, permission_id),

    CONSTRAINT fk_roles_permissions_role_id FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_roles_permissions_permission_id FOREIGN KEY (permission_id) REFERENCES permissions(id)
);