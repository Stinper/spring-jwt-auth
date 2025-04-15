package me.stinper.jwtauth.service.security.contract;

import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import org.springframework.lang.NonNull;

public interface RoleSecurityService {
    boolean isAllowedToFindAllRoles(@NonNull JwtAuthUserDetails user);

    boolean isAllowedToFindRoleByName(@NonNull String roleName, @NonNull JwtAuthUserDetails user);

    boolean isAllowedToCreateRole(@NonNull JwtAuthUserDetails user);

    boolean isAllowedToUpdateRolePermissions(@NonNull String roleName, @NonNull JwtAuthUserDetails user);

    boolean isAllowedToDeleteRoleByName(@NonNull String roleName, @NonNull JwtAuthUserDetails user);
}
