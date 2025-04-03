package me.stinper.jwtauth.mapping;

import me.stinper.jwtauth.dto.role.RoleCreationRequest;
import me.stinper.jwtauth.dto.role.RoleDto;
import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.mapping.impl.RoleMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Unit Tests for Role model mapper")
class RoleMapperUnitTest {
    private RoleMapper roleMapper;

    @BeforeEach
    void setUp() {
        this.roleMapper = new RoleMapperImpl();
    }

    @Test
    @DisplayName(
            """
            [#toRoleDto]: Check that the method correctly converts all fields of an object of
            type [Role] into an object of type [RoleDto]
            """
    )
    void toRoleDtoConvertsAllFieldsCorrectly() {
        //GIVEN
        Role role = new Role(1L, "ROLE_ADMIN");

        //WHEN
        RoleDto roleDto = this.roleMapper.toRoleDto(role);

        //THEN
        assertThat(roleDto.id()).isEqualTo(role.getId());
        assertThat(roleDto.roleName()).isEqualTo(role.getRoleName());
    }

    @Test
    @DisplayName(
            """
                    [#toRole]: Check that the method correctly converts all fields of an object of
                    type [RoleCreationRequest] into an object of type [Role]
                    """
    )
    void toRoleConvertsAllFieldsCorrectly() {
        //GIVEN
        RoleCreationRequest roleCreationRequest = RoleCreationRequest.builder()
                .roleName("ROLE_ADMIN")
                .build();

        //WHEN
        Role mappedRole = this.roleMapper.toRole(roleCreationRequest);

        //THEN
        assertThat(mappedRole.getId()).isNull(); //Must be generated automatically on the database level
        assertThat(mappedRole.getRoleName()).isEqualTo(roleCreationRequest.roleName());
    }
}
