package me.stinper.jwtauth.service.entity.contract;

import me.stinper.jwtauth.dto.user.UserCreationRequest;
import me.stinper.jwtauth.dto.user.UserDto;
import me.stinper.jwtauth.service.entity.support.UserFilterStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.UUID;


/**
 * Сервис, содержащий операции с моделью пользователя (User)
 * @see me.stinper.jwtauth.entity.User
 * @see me.stinper.jwtauth.dto.user.UserDto
 * @see me.stinper.jwtauth.dto.user.UserCreationRequest
 */
public interface UserService {
    Page<UserDto> findAll(@NonNull Pageable pageable, @NonNull UserFilterStrategy userFilterStrategy);

    Optional<UserDto> findByUUID(@NonNull UUID uuid);

    UserDto create(@NonNull UserCreationRequest userCreationRequest);

    void deleteByUUID(@NonNull UUID uuid);
}
