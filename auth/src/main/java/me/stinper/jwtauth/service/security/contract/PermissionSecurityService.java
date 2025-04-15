package me.stinper.jwtauth.service.security.contract;

import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import org.springframework.lang.NonNull;

public interface PermissionSecurityService {
    boolean isAllowedToFindAllPermissions(@NonNull JwtAuthUserDetails user);

    boolean isAllowedToFindPermissionById(@NonNull Long id, @NonNull JwtAuthUserDetails user);

    boolean isAllowedToCreatePermission(@NonNull JwtAuthUserDetails user);

    boolean isAllowedToUpdatePermissionDescription(@NonNull Long permissionId, @NonNull JwtAuthUserDetails user);

    boolean isAllowedToDeletePermission(@NonNull Long id, @NonNull JwtAuthUserDetails user);
}
