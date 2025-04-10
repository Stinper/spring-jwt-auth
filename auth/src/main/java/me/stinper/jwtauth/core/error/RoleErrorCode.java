package me.stinper.jwtauth.core.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RoleErrorCode {
    ROLE_NAME_NOT_UNIQUE("roles.role-name-not-unique"),
    RELATED_USER_EXISTS("roles.related-user-exists"),
    INVALID_PERMISSION_CODE("roles.permissions.invalid-code");

    private final String code;
}
