package me.stinper.jwtauth.service.entity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.dto.user.UserCreationRequest;
import me.stinper.jwtauth.dto.user.UserDto;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.exception.EntityValidationException;
import me.stinper.jwtauth.exception.NoSuchPropertyException;
import me.stinper.jwtauth.exception.ResourceNotFoundException;
import me.stinper.jwtauth.mapping.UserMapper;
import me.stinper.jwtauth.repository.UserRepository;
import me.stinper.jwtauth.service.authentication.contract.JwtService;
import me.stinper.jwtauth.service.entity.contract.UserService;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import me.stinper.jwtauth.validation.UserCreationValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserCreationValidator userCreationValidator;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public Page<UserDto> findAll(Pageable pageable) {
        try {
            log.atDebug().log(() -> "[#findAll]: Начало выполнения метода. Запрос на пагинацию: " + pageable.toString());

            Page<UserDto> users = userRepository.findAllByDeactivatedAtIsNull(pageable)
                    .map(userMapper::toUserDto);

            log.atDebug().log(
                    "[#findAll]: Выполнение метода завершено. Всего пользователей: {}, всего страниц: {}",
                    users.getTotalElements(),
                    users.getTotalPages()
            );

            return users;
        }
        catch (PropertyReferenceException pre) {
            log.atWarn().log("[#findAll]: Свойство с именем '{}' не существует", pre.getPropertyName());
            throw new NoSuchPropertyException(pre);
        }
    }

    @Override
    public Optional<UserDto> findByUUID(@NonNull UUID uuid) {
        log.atDebug().log("[#findByUUID]: Начало выполнения метода. UUID: {}", uuid);

        Optional<UserDto> user = userRepository.findById(uuid)
                .map(userMapper::toUserDto);

        user.ifPresentOrElse(
                (presentedUser) -> log.atDebug().log("[#findByUUID]: Пользователь с UUID '{}' найден", uuid),
                () -> log.atDebug().log("[#findByUUID]: Пользователь с UUID '{}' не найден", uuid)
        );

        return user;
    }

    @Override
    public UserDto create(@NonNull UserCreationRequest userCreationRequest) {
        log.atDebug().log(() -> "[#create]: Начало выполнения метода. Информация о запросе: " + userCreationRequest);
        Errors validationErrors = userCreationValidator.validateObject(userCreationRequest);

        if (validationErrors.hasFieldErrors()) {
            log.atWarn().log(() -> "[#create]: Ошибка валидации запроса на создание пользователя: " + validationErrors.getFieldErrors());
            throw new EntityValidationException(validationErrors.getFieldErrors());
        }

        log.atDebug().log("[#create]: Валидация запроса на создания пользователя успешно пройдена");

        User user = userMapper.toUser(userCreationRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        log.atDebug().log("[#create]: Пароль пользователя успешно захеширован");

        user = userRepository.save(user);

        log.atInfo().log("[#create]: Пользователь успешно создан. Информация о запросе: {}", userCreationRequest);

        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void deleteByUUID(@NonNull UUID uuid) {
        log.atDebug().log("[#deleteByUUID]: Начало выполнения метода. UUID: '{}'", uuid);

        User user = userRepository.findById(uuid)
                .orElseThrow(
                        () -> new ResourceNotFoundException("messages.user.not-found.uuid", uuid)
                );

        log.atDebug().log("Пользователь с UUID '{}' найден", uuid);

        if (user.getDeactivatedAt() != null) {
            log.atDebug().log("[#deleteByUUID]: Учетная запись с UUID '{}' уже деактивирована. Действий не требуется", uuid);
            return;
        }

        user.setDeactivatedAt(Instant.now());
        userRepository.save(user);
        jwtService.invalidateRefreshTokens(user);

        log.atInfo().log("[#deleteByUUID]: Учетная запись пользователя с UUID '{}' успешно деактивирована", uuid);
    }

}
