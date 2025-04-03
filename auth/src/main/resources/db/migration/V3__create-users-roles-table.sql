CREATE TABLE IF NOT EXISTS users_roles (
    id bigint primary key generated always as identity,
    user_id uuid not null,
    role_id bigint not null,

    -- Композитный ключ, который не позволит присвоить одному пользователю одну и ту же роль более 1 раза
    CONSTRAINT ck_user_id_role_id UNIQUE (user_id, role_id),

    CONSTRAINT fk_users_roles_user_id FOREIGN KEY (user_id) REFERENCES users(uuid),
    CONSTRAINT fk_users_roles_role_id FOREIGN KEY (role_id) REFERENCES roles(id)
);