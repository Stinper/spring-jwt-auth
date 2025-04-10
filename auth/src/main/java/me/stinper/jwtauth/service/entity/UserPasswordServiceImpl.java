package me.stinper.jwtauth.service.entity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stinper.jwtauth.core.security.jwt.JwtAuthUserDetails;
import me.stinper.jwtauth.dto.user.PasswordChangeRequest;
import me.stinper.jwtauth.entity.User;
import me.stinper.jwtauth.exception.EntityValidationException;
import me.stinper.jwtauth.repository.UserRepository;
import me.stinper.jwtauth.service.authentication.contract.JwtService;
import me.stinper.jwtauth.service.entity.contract.UserPasswordService;
import me.stinper.jwtauth.validation.PasswordChangeValidator;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPasswordServiceImpl implements UserPasswordService {
    private final PasswordChangeValidator passwordChangeValidator;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    @Transactional
    public void changePassword(@NonNull PasswordChangeRequest passwordChangeRequest, @NonNull JwtAuthUserDetails userDetails) {

        log.atDebug().log("[#changePassword]: Начало выполнение метода. Пользователь: {}", userDetails.getUuid());

        Errors validationErrors = passwordChangeValidator.validateObject(passwordChangeRequest);

        if (validationErrors.hasFieldErrors()) {
            log.atWarn().log("[#changePassword]: Попытка смены пароля для пользователя '{}' не удалась", userDetails.getUuid());
            throw new EntityValidationException(validationErrors.getFieldErrors());
        }

        User user = (User) userDetails;

        user.setPassword(
                passwordEncoder.encode(passwordChangeRequest.newPassword())
        );

        userRepository.save(user);
        jwtService.invalidateRefreshTokens(user);

        log.atInfo().log("[#changePassword]: Пользователь '{}' успешно изменил свой пароль", user.getUuid());
    }
}
