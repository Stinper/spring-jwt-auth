package me.stinper.jwtauth.core.security;

import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Используется для проверки наличия прав доступа, которые могут быть получены из различных
 * источников, в зависимости от реализации фильтров безопасности. <br>
 * Например, можно проверять права доступа у модели User (реализация по-умолчанию), или права, вшитые в JWT-токен
 *
 * @see AbstractAuthorityChecker
 * @see UserModelAuthorityChecker
 */
public interface AuthorityChecker {

    boolean hasAuthority(@NonNull UserDetails userDetails, @NonNull String name);

    boolean isAdmin(@NonNull UserDetails user);

    default boolean isAdminOrHasPermission(@NonNull UserDetails user, @NonNull String permission) {
        return this.isAdmin(user) || this.hasAuthority(user, permission);
    }

    default boolean hasAllAuthorities(@NonNull UserDetails user, @NonNull String... authorities) {
        for (String authority : authorities) {
            if (!this.hasAuthority(user, authority))
                return false;
        }

        return true;
    }

    default boolean hasAnyAuthorities(@NonNull UserDetails user, @NonNull String... authorities) {
        for (String authority : authorities) {
            if (this.hasAuthority(user, authority))
                return true;
        }

        return false;
    }
}
