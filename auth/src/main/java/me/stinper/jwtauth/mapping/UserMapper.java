package me.stinper.jwtauth.mapping;

import me.stinper.jwtauth.dto.user.UserCreationRequest;
import me.stinper.jwtauth.dto.user.UserDto;
import me.stinper.jwtauth.entity.User;

public interface UserMapper {
    UserDto toUserDto(User user);

    User toUser(UserCreationRequest userCreationRequest);
}
