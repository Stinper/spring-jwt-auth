package me.stinper.jwtauth.service.security;

import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.core.security.AuthorityChecker;
import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import me.stinper.jwtauth.service.security.contract.PermissionSecurityService;
import org.springframework.lang.NonNull;

@RequiredArgsConstructor
public class PermissionSecurityServiceImpl implements PermissionSecurityService {
    private final AuthorityChecker authorityChecker;

    @Override
    public boolean isAllowedToFindAllPermissions(@NonNull JwtAuthUserDetails user) {
        return authorityChecker.isAdminOrHasPermission(user, "permission.read.find-all-permissions");
    }

    @Override
    public boolean isAllowedToFindPermissionById(@NonNull Long id, @NonNull JwtAuthUserDetails user) {
        return authorityChecker.isAdminOrHasPermission(user, "permission.read.find-by-id");
    }

    @Override
    public boolean isAllowedToCreatePermission(@NonNull JwtAuthUserDetails user) {
        return authorityChecker.isAdminOrHasPermission(user, "permission.create.create-permission");
    }

    @Override
    public boolean isAllowedToUpdatePermissionDescription(@NonNull Long permissionId, @NonNull JwtAuthUserDetails user) {
        return authorityChecker.isAdminOrHasPermission(user, "permission.update.description");
    }

    @Override
    public boolean isAllowedToDeletePermission(@NonNull Long id, @NonNull JwtAuthUserDetails user) {
        return authorityChecker.isAdminOrHasPermission(user, "permission.delete.delete-by-id");
    }
}
