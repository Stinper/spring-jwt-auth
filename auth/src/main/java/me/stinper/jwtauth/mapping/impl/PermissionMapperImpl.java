package me.stinper.jwtauth.mapping.impl;

import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.dto.permission.PermissionCreationRequest;
import me.stinper.jwtauth.dto.permission.PermissionDto;
import me.stinper.jwtauth.entity.Permission;
import me.stinper.jwtauth.mapping.PermissionMapper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PermissionMapperImpl implements PermissionMapper {
    @Override
    public PermissionDto toPermissionDto(Permission permission) {
        PermissionDto permissionDto = PermissionDto.builder()
                .id(permission.getId())
                .permission(permission.getPermission())
                .description(permission.getDescription())
                .build();

        log.atDebug().log(() -> "[#toPermissionDto]: Выполнен маппинг Permission -> PermissionDto. " +
                "\nPermission: " + permission + "\nPermissionDto: " + permissionDto);

        return permissionDto;
    }

    @Override
    public Permission toPermission(PermissionCreationRequest permissionCreationRequest) {
        Permission permission = Permission.builder()
                .permission(permissionCreationRequest.permission())
                .description(permissionCreationRequest.description())
                .build();

        log.atDebug().log(() -> "[#toPermissionDto]: Выполнен маппинг PermissionCreationRequest -> Permission. " +
                "\nPermissionCreationRequest: " + permissionCreationRequest + "\nPermission: " + permission);

        return permission;
    }
}
