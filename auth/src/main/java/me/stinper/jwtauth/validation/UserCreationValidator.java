package me.stinper.jwtauth.validation;

import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.core.error.UserErrorCode;
import me.stinper.jwtauth.dto.user.UserCreationRequest;
import me.stinper.jwtauth.exception.ValidatorUnsupportedTypeException;
import me.stinper.jwtauth.repository.UserRepository;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Валидатор, который отвечает за database-related проверки при создании модели пользователя,
 * такие как проверка уникальности электронной почты
 */
@Component
@RequiredArgsConstructor
public class UserCreationValidator implements Validator {
    private final UserRepository userRepository;
    private final MessageSourceHelper messageSourceHelper;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return UserCreationRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        if (!this.supports(target.getClass()))
            throw new ValidatorUnsupportedTypeException();

        UserCreationRequest userCreationRequest = (UserCreationRequest) target;

        if (userRepository.existsByEmailIgnoreCase(userCreationRequest.email())) {
            errors.rejectValue(
                    "email",
                    UserErrorCode.EMAIL_NOT_UNIQUE.getCode(),
                    messageSourceHelper.getLocalizedMessage(
                            "messages.user.validation.fields.email.not-unique",
                            userCreationRequest.email()
                    )
            );
        }
    }
}
