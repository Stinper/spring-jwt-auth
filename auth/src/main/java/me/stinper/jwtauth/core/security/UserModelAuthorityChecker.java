package me.stinper.jwtauth.core.security;

import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class UserModelAuthorityChecker extends AbstractAuthorityChecker {
    public UserModelAuthorityChecker(AdminRoleNameHolder adminRoleNameHolder) {
        super(adminRoleNameHolder);
    }

    @Override
    public boolean hasAuthority(@NonNull UserDetails userDetails, @NonNull String name) {
        Collection<? extends GrantedAuthority> userRoles = userDetails.getAuthorities();

        return userRoles
                .stream()
                .anyMatch(role -> role.getAuthority().equals(name));
    }
}
