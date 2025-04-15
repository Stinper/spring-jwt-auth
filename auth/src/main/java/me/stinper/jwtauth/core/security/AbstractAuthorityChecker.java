package me.stinper.jwtauth.core.security;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public abstract class AbstractAuthorityChecker implements AuthorityChecker {
    private final AdminRoleNameHolder adminRoleNameHolder;

    @Override
    public boolean isAdmin(@NonNull UserDetails user) {
        return this.hasAuthority(user, adminRoleNameHolder.adminRoleName());
    }
}
