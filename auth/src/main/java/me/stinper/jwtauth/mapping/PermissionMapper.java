package me.stinper.jwtauth.mapping;

import me.stinper.jwtauth.dto.permission.PermissionCreationRequest;
import me.stinper.jwtauth.dto.permission.PermissionDto;
import me.stinper.jwtauth.entity.Permission;

public interface PermissionMapper {
    PermissionDto toPermissionDto(Permission permission);

    Permission toPermission(PermissionCreationRequest permissionCreationRequest);
}
