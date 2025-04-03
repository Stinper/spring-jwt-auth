package me.stinper.jwtauth.mapping.impl;

import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.dto.role.RoleCreationRequest;
import me.stinper.jwtauth.dto.role.RoleDto;
import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.mapping.RoleMapper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RoleMapperImpl implements RoleMapper {
    @Override
    public RoleDto toRoleDto(Role role) {
        RoleDto roleDto = RoleDto.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .build();

        log.atDebug().log(() -> "[#toRoleDto]: Выполнен маппинг Role -> RoleDto." +
                "\nRole: " + role + "\nRoleDto: " + roleDto);

        return roleDto;
    }

    @Override
    public Role toRole(RoleCreationRequest roleCreationRequest) {
        Role role = Role.builder()
                .roleName(roleCreationRequest.roleName())
                .build();

        log.atDebug().log(() -> "[#toRole]: Выполнен маппинг RoleCreationRequest -> Role." +
                "\nRoleCreationRequest: " + roleCreationRequest + "\nRole: " + role);

        return role;
    }
}
