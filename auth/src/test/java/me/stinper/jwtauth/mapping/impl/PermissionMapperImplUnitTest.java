package me.stinper.jwtauth.mapping.impl;

import me.stinper.jwtauth.dto.permission.PermissionCreationRequest;
import me.stinper.jwtauth.dto.permission.PermissionDto;
import me.stinper.jwtauth.entity.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for PermissionMapperImpl class")
class PermissionMapperImplUnitTest {
    private PermissionMapperImpl permissionMapper;

    @BeforeEach
    void setUp() {
        this.permissionMapper = new PermissionMapperImpl();
    }


    @Test
    void toPermissionDto_convertsAllFieldsCorrectly() {
        //GIVEN
        final Permission permission = Permission.builder()
                .id(1L)
                .permission("some.permission")
                .description("Permission description")
                .build();

        //WHEN
        PermissionDto permissionDto = permissionMapper.toPermissionDto(permission);

        //THEN
        assertThat(permissionDto.id()).isEqualTo(permission.getId());
        assertThat(permissionDto.permission()).isEqualTo(permission.getPermission());
        assertThat(permissionDto.description()).isEqualTo(permission.getDescription());
    }


    @Test
    void toPermission_convertsAllFieldsCorrectly() {
        //GIVEN
        final PermissionCreationRequest permissionCreationRequest = new PermissionCreationRequest(
                "some.permission", "Description"
        );

        //WHEN
        Permission permission = permissionMapper.toPermission(permissionCreationRequest);

        //THEN
        assertThat(permission.getId()).isNull();
        assertThat(permission.getPermission()).isEqualTo(permissionCreationRequest.permission());
        assertThat(permission.getDescription()).isEqualTo(permissionCreationRequest.description());
    }
}