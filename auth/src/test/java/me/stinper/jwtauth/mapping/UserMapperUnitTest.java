package me.stinper.jwtauth.mapping;


import me.stinper.jwtauth.dto.user.UserCreationRequest;
import me.stinper.jwtauth.dto.user.UserDto;
import me.stinper.jwtauth.entity.Role;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.mapping.impl.RoleMapperImpl;
import me.stinper.jwtauth.mapping.impl.UserMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Unit Tests for User model mapper")
class UserMapperUnitTest {
    private UserMapper userMapper;
    private RoleMapper roleMapper;

    @BeforeEach
    void setUp() {
        this.roleMapper = new RoleMapperImpl();
        this.userMapper = new UserMapperImpl(roleMapper);
    }

    @ParameterizedTest
    @DisplayName(
            """
                    [#toUserDto]: Check that the method correctly converts all fields
                    of an object of type [User] into an object of type [UserDto]
                    """
    )
    @MethodSource("userObjectsSource")
    void toUserDtoConvertsAllFieldsCorrectly(User user) {
        //WHEN
        UserDto userDto = this.userMapper.toUserDto(user);

        //THEN
        assertThat(userDto.uuid()).isEqualTo(user.getUuid());
        assertThat(userDto.email()).isEqualTo(user.getEmail());
        assertThat(userDto.isEmailVerified()).isEqualTo(user.getIsEmailVerified());
        assertThat(userDto.registeredAt()).isEqualTo(user.getRegisteredAt());

        assertThat(userDto.roles()).isEqualTo(
                user.getRoles().stream()
                        .map(roleMapper::toRoleDto)
                        .toList()
        );
    }

    @Test
    @DisplayName(
            """
                    [#toUser]: Check that the method correctly converts all fields of an object of
                    type [UserCreationRequest] into an object of type [User]
                    """
    )
    void toUserConvertsAllFieldsCorrectly() {
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


    private static Stream<Arguments> userObjectsSource() {
        User.UserBuilder baseUser = User.builder()
                .uuid(UUID.randomUUID())
                .email("user@gmail.com");

        return Stream.of(
                Arguments.of(baseUser
                        .roles(List.of(
                                new Role(1L, "ROLE_ADMIN")
                        ))
                        .build()
                ),
                Arguments.of(baseUser
                        .roles(List.of(
                                new Role(1L, "ROLE_ADMIN"),
                                new Role(2L, "ROLE_MANAGER")
                        ))
                        .build()
                ),
                Arguments.of(baseUser
                        .isEmailVerified(true)
                        .build()
                ),
                Arguments.of(baseUser
                        .build()
                )
        );
    }

}
