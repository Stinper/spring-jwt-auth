package me.stinper.jwtauth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.FieldError;

import java.util.List;

/**
 * Выбрасывается при ошибке валидации <b>сущности в целом</b>, т.е. такой ошибки, которая связана
 * с конкретным полем конкретной сущности. <br>
 * Пример: валидация объекта-запроса на создание учетной записи пользователя {@link me.stinper.jwtauth.dto.user.UserCreationRequest}
 */
@Getter
@RequiredArgsConstructor
public class EntityValidationException extends RuntimeException {
    private final List<FieldError> fieldErrors;

    public EntityValidationException(String message, List<FieldError> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public EntityValidationException(String message, Throwable cause, List<FieldError> fieldErrors) {
        super(message, cause);
        this.fieldErrors = fieldErrors;
    }
}
