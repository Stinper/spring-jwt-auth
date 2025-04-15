package me.stinper.jwtauth.mapping.impl;

import me.stinper.jwtauth.dto.permission.PermissionDto;
import me.stinper.jwtauth.dto.role.RoleCreationRequest;
import me.stinper.jwtauth.dto.role.RoleDto;
import me.stinper.jwtauth.entity.Permission;
import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.mapping.PermissionMapper;
import me.stinper.jwtauth.mapping.impl.RoleMapperImpl;
import me.stinper.jwtauth.repository.PermissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for Role model mapper")
@ExtendWith(MockitoExtension.class)
class RoleMapperImplUnitTest {
    @Mock private PermissionRepository permissionRepository;
    @Mock private PermissionMapper permissionMapper;

    @InjectMocks
    private RoleMapperImpl roleMapper;

    @Test
    @DisplayName(
            """
            [#toRoleDto]: Check that the method correctly converts all fields of an object of
            type [Role] into an object of type [RoleDto]
            """
    )
    void toRoleDto_whenRoleHasPermissions_thenConvertsAllFieldsCorrectly() {
        //GIVEN
        final Permission firstPermission = new Permission(1L, "first.permission", null);
        final Permission secondPermission = new Permission(2L, "second.permission", "Description");

        final PermissionDto firstPermissionDto = new PermissionDto(1L, "first.permission", null);
        final PermissionDto secondPermissionDto = new PermissionDto(2L, "second.permission", "Description");

        final Role role = new Role(1L, "ROLE_ADMIN", "Администратор", Set.of(firstPermission, secondPermission));

        when(permissionMapper.toPermissionDto(firstPermission)).thenReturn(firstPermissionDto);
        when(permissionMapper.toPermissionDto(secondPermission)).thenReturn(secondPermissionDto);

        //WHEN
        RoleDto roleDto = this.roleMapper.toRoleDto(role);

        //THEN
        assertThat(roleDto.id()).isEqualTo(role.getId());
        assertThat(roleDto.roleName()).isEqualTo(role.getRoleName());
        assertThat(roleDto.prefix()).isEqualTo(role.getPrefix());
        assertThat(roleDto.permissions()).containsExactlyInAnyOrder(firstPermissionDto, secondPermissionDto);

        verify(permissionMapper).toPermissionDto(firstPermission);
        verify(permissionMapper).toPermissionDto(secondPermission);

        verifyNoInteractions(permissionRepository);
    }


    @Test
    void toRoleDto_whenRoleHasNoPermissions_thenConvertsAllFieldsCorrectly() {
        //GIVEN
        final Role role = new Role(1L, "ROLE_ADMIN", "Администратор", Collections.emptySet());

        //WHEN
        RoleDto roleDto = this.roleMapper.toRoleDto(role);

        //THEN
        assertThat(roleDto.id()).isEqualTo(role.getId());
        assertThat(roleDto.roleName()).isEqualTo(role.getRoleName());
        assertThat(roleDto.prefix()).isEqualTo(role.getPrefix());
        assertThat(roleDto.permissions()).isEmpty();

        verifyNoInteractions(permissionMapper, permissionRepository);
    }


    @Test
    @DisplayName(
            """
                    [#toRole]: Check that the method correctly converts all fields of an object of
                    type [RoleCreationRequest] into an object of type [Role]
                    """
    )
    void toRole_whenRoleCreationRequestContainsPermissions_thenConvertsAllFieldsCorrectly() {
        //GIVEN
        final Permission firstPermission = new Permission(1L, "first.permission", null);
        final Permission secondPermission = new Permission(2L, "second.permission", "Description");

        Set<String> permissions = Set.of("first.permission", "second.permission");

        final RoleCreationRequest roleCreationRequest = RoleCreationRequest.builder()
                .roleName("ROLE_ADMIN")
                .prefix("Администратор")
                .permissions(permissions)
                .build();

        when(permissionRepository.findAllByPermissionIn(permissions)).thenReturn(Set.of(firstPermission, secondPermission));

        //WHEN
        Role mappedRole = this.roleMapper.toRole(roleCreationRequest);

        //THEN
        assertThat(mappedRole.getId()).isNull(); //Must be generated automatically on the database level
        assertThat(mappedRole.getRoleName()).isEqualTo(roleCreationRequest.roleName());
        assertThat(mappedRole.getPrefix()).isEqualTo(roleCreationRequest.prefix());
        assertThat(mappedRole.getPermissions()).containsExactlyInAnyOrder(firstPermission, secondPermission);

        verify(permissionRepository).findAllByPermissionIn(permissions);
        verifyNoInteractions(permissionMapper);
    }
}
