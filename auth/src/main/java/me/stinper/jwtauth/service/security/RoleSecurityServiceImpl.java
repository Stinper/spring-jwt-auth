package me.stinper.jwtauth.service.security;

import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.core.security.AuthorityChecker;
import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import me.stinper.jwtauth.service.security.contract.RoleSecurityService;
import org.springframework.lang.NonNull;

@RequiredArgsConstructor
public class RoleSecurityServiceImpl implements RoleSecurityService {
    private final AuthorityChecker authorityChecker;

    @Override
    public boolean isAllowedToFindAllRoles(@NonNull JwtAuthUserDetails user) {
        return authorityChecker.isAdminOrHasPermission(user, "role.read.find-all-roles");
    }

    @Override
    public boolean isAllowedToFindRoleByName(@NonNull String roleName, @NonNull  JwtAuthUserDetails user) {
        return authorityChecker.isAdminOrHasPermission(user, "role.read.find-role-by-name");

    }

    @Override
    public boolean isAllowedToCreateRole(@NonNull  JwtAuthUserDetails user) {
        return authorityChecker.isAdminOrHasPermission(user, "role.create.create-role");

    }

    @Override
    public boolean isAllowedToUpdateRolePermissions(@NonNull  String roleName, @NonNull  JwtAuthUserDetails user) {
        return authorityChecker.isAdminOrHasPermission(user, "role.update.partial.permissions-list");
    }

    @Override
    public boolean isAllowedToDeleteRoleByName(@NonNull String roleName, @NonNull  JwtAuthUserDetails user) {
        return authorityChecker.isAdminOrHasPermission(user, "role.delete.delete-role-by-name");
    }
}
