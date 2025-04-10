package me.stinper.jwtauth.mapping.impl;


import me.stinper.jwtauth.dto.permission.PermissionDto;
import me.stinper.jwtauth.dto.role.RoleDto;
import me.stinper.jwtauth.dto.user.UserCreationRequest;
import me.stinper.jwtauth.dto.user.UserDto;
import me.stinper.jwtauth.entity.Permission;
import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.mapping.RoleMapper;
import me.stinper.jwtauth.mapping.impl.UserMapperImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for User model mapper")
@ExtendWith(MockitoExtension.class)
class UserMapperUnitTest {
    @Mock private RoleMapper roleMapper;

    @InjectMocks
    private UserMapperImpl userMapper;

    @Test
    @DisplayName(
            """
                    [#toUserDto]: Check that the method correctly converts all fields
                    of an object of type [User] into an object of type [UserDto]
                    """
    )
    void toUserDto_convertsAllFieldsCorrectly() {
        //GIVEN
        final Role firstRole = new Role(1L, "ROLE_MANAGER", "Менеджер", Collections.emptyList());
        final Role secondRole = new Role(2L, "ROLE_ADMIN", "Администратор",
                List.of(
                        new Permission(1L, "some.permission", null)
                )
        );

        final RoleDto firstRoleDto = new RoleDto(1L, "ROLE_MANAGER", "Менеджер", Collections.emptyList());
        final RoleDto secondRoleDto = new RoleDto(2L, "ROLE_ADMIN", "Администратор",
                List.of(
                        new PermissionDto(1L, "some.permission", null)
                )
        );

        final User user = User.builder()
                .uuid(UUID.randomUUID())
                .email("user@gmail.com")
                .password("123")
                .roles(List.of(firstRole, secondRole))
                .build();

        when(roleMapper.toRoleDto(firstRole)).thenReturn(firstRoleDto);
        when(roleMapper.toRoleDto(secondRole)).thenReturn(secondRoleDto);

        //WHEN
        UserDto userDto = this.userMapper.toUserDto(user);

        //THEN
        assertThat(userDto.uuid()).isEqualTo(user.getUuid());
        assertThat(userDto.email()).isEqualTo(user.getEmail());
        assertThat(userDto.isEmailVerified()).isEqualTo(user.getIsEmailVerified());
        assertThat(userDto.registeredAt()).isEqualTo(user.getRegisteredAt());
        assertThat(userDto.roles()).containsExactly(firstRoleDto, secondRoleDto);

        verify(roleMapper).toRoleDto(firstRole);
        verify(roleMapper).toRoleDto(secondRole);
        verifyNoMoreInteractions(roleMapper);
    }

    @Test
    @DisplayName(
            """
                    [#toUser]: Check that the method correctly converts all fields of an object of
                    type [UserCreationRequest] into an object of type [User]
                    """
    )
    void toUser_convertsAllFieldsCorrectly() {
        //GIVEN
        UserCreationRequest userCreationRequest = UserCreationRequest.builder()
                .email("user@gmail.com")
                .password("123")
                .build();

        //WHEN
        User mappedUser = this.userMapper.toUser(userCreationRequest);

        //THEN
        assertThat(mappedUser.getUuid()).isNull(); //Must be generated automatically on the database level
        assertThat(mappedUser.getEmail()).isEqualTo(userCreationRequest.email());
        assertThat(mappedUser.getPassword()).isEqualTo(userCreationRequest.password());
    }
}
