package me.stinper.jwtauth.utils;

import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.entity.User;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Slf4j
public final class SecurityUtils {
    private SecurityUtils() {}

    /**
     * Извлекает Principal из контекста безопасности ({@link SecurityContextHolder}), и пытается привести его к типу
     * {@link User}
     * @return объект пользователя
     * @throws IllegalStateException если извлеченный из контекста безопасности объект нельзя привести к типу {@link User}
     */
    public static User getCurrentUser() throws IllegalStateException {
        log.atDebug().log("[#getCurrentUser]: Начало выполнения метода");

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User user) {
            log.atDebug().log("[#getCurrentUser]: Из контекста безопасности извлечен объект [User] с UUID '{}'", user.getUuid() );
            return user;
        }

        throw new IllegalStateException("Объект, содержащийся в контексте безопасности не является объектом типа [User]");
    }

    public static boolean hasAuthority(@NonNull UserDetails userDetails, @NonNull String name) {
        Collection<? extends GrantedAuthority> userRoles = userDetails.getAuthorities();

        return userRoles
                .stream()
                .anyMatch(role -> role.getAuthority().equals(name));
    }
}
