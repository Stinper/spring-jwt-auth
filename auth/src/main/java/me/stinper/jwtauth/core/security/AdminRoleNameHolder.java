package me.stinper.jwtauth.core.security;

/**
 * Предназначен для хранения и предоставления имени роли администратора. Этот интерфейс нужен для того, чтобы не хардкодить
 * имя роли админа в различных проверках прав доступа
 */
public interface AdminRoleNameHolder {
    String adminRoleName();
}
