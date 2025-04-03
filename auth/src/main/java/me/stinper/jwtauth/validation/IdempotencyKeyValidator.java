package me.stinper.jwtauth.validation;

import lombok.RequiredArgsConstructor;
import me.stinper.jwtauth.core.error.IdempotencyKeyErrorCode;
import me.stinper.jwtauth.exception.ValidatorUnsupportedTypeException;
import me.stinper.jwtauth.repository.IdempotencyKeyRepository;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdempotencyKeyValidator implements Validator {
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final MessageSourceHelper messageSourceHelper;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return UUID.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        if (!this.supports(target.getClass()))
            throw new ValidatorUnsupportedTypeException();

        UUID idempotencyKey = (UUID) target;

        if (idempotencyKeyRepository.existsByKey(idempotencyKey)) {
            errors.reject(
                    IdempotencyKeyErrorCode.KEY_NOT_UNIQUE.getCode(),
                    messageSourceHelper.getLocalizedMessage(
                            "messages.idempotency-key.fields.key.not-unique",
                            idempotencyKey
                    )
            );
        }
    }
}
