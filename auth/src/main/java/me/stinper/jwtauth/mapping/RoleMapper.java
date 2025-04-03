package me.stinper.jwtauth.mapping;

import me.stinper.jwtauth.dto.role.RoleCreationRequest;
import me.stinper.jwtauth.dto.role.RoleDto;
import me.stinper.jwtauth.entity.Role;

public interface RoleMapper {
    RoleDto toRoleDto(Role role);

    Role toRole(RoleCreationRequest roleCreationRequest);
}
