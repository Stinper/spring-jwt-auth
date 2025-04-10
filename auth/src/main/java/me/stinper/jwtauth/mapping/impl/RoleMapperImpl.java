package me.stinper.jwtauth.mapping.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.dto.role.RoleCreationRequest;
import me.stinper.jwtauth.dto.role.RoleDto;
import me.stinper.jwtauth.entity.Permission;
import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.mapping.PermissionMapper;
import me.stinper.jwtauth.mapping.RoleMapper;
import me.stinper.jwtauth.repository.PermissionRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoleMapperImpl implements RoleMapper {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public RoleDto toRoleDto(Role role) {
        RoleDto roleDto = RoleDto.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .prefix(role.getPrefix())
                .permissions(
                        Optional.ofNullable(role.getPermissions())
                                .map(p ->
                                        p.stream()
                                                .map(permissionMapper::toPermissionDto)
                                                .toList()
                                )
                                .orElse(Collections.emptyList())
                )
                .build();

        log.atDebug().log(() -> "[#toRoleDto]: Выполнен маппинг Role -> RoleDto." +
                "\nRole: " + role + "\nRoleDto: " + roleDto);

        return roleDto;
    }

    @Override
    public Role toRole(RoleCreationRequest roleCreationRequest) {

        Role.RoleBuilder roleBuilder = Role.builder()
                .roleName(roleCreationRequest.roleName())
                .prefix(roleCreationRequest.prefix());

        if (roleCreationRequest.permissions() != null) {
            List<Permission> permissions = permissionRepository.findAllByPermissionIn(roleCreationRequest.permissions());

            roleBuilder.permissions(permissions);
        }

        Role role = roleBuilder.build();

        log.atDebug().log(() -> "[#toRole]: Выполнен маппинг RoleCreationRequest -> Role." +
                "\nRoleCreationRequest: " + roleCreationRequest + "\nRole: " + role);

        return role;
    }
}
