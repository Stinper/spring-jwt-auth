package me.stinper.jwtauth.core.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PermissionErrorCode {
    PERMISSION_NOT_UNIQUE("permissions.permission-not-unique"),
    RELATED_ROLE_EXISTS("permissions.related-role-exists");

    private final String code;
}
