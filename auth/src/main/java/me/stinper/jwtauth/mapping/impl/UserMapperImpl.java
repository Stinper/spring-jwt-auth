package me.stinper.jwtauth.mapping.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.dto.user.UserCreationRequest;
import me.stinper.jwtauth.dto.user.UserDto;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.mapping.RoleMapper;
import me.stinper.jwtauth.mapping.UserMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserMapperImpl implements UserMapper {
    private final RoleMapper roleMapper;

    @Override
    public UserDto toUserDto(User user) {
        UserDto userDto = UserDto.builder()
                .uuid(user.getUuid())
                .email(user.getEmail())
                .registeredAt(user.getRegisteredAt())
                .isEmailVerified(user.getIsEmailVerified())
                .deactivatedAt(user.getDeactivatedAt())
                .roles(
                        Optional.ofNullable(user.getRoles())
                                .map(roles ->
                                        roles.stream()
                                                .map(roleMapper::toRoleDto)
                                                .toList()
                                )
                                .orElse(Collections.emptyList())
                )
                .build();

        log.atDebug().log(() -> "[#toUserDto]: Выполнен маппинг User -> UserDto." +
                "\nUser: " + user + "\nUserDto: " + userDto);

        return userDto;
    }

    @Override
    public User toUser(UserCreationRequest userCreationRequest) {
        User user = User.builder()
                .email(userCreationRequest.email())
                .password(userCreationRequest.password())
                .build();

        log.atDebug().log(() -> "[#toUser]: Выполнен маппинг UserCreationRequest -> User. " +
                "\nUserCreationRequest: " + userCreationRequest + "\nUser: " + user);

        return user;
    }
}
