package me.stinper.jwtauth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.ObjectError;

import java.util.List;

/**
 * Выбрасывается при ошибке валидации <b>единичного значения</b>, а не сущности в целом.
 * Пример: валидация значения UUID на уникальность в рамках таблицы
 */
@Getter
@RequiredArgsConstructor
public class ObjectValueValidationException extends RuntimeException {
    private final List<ObjectError> objectErrors;

    public ObjectValueValidationException(String message, List<ObjectError> objectErrors) {
        super(message);
        this.objectErrors = objectErrors;
    }

    public ObjectValueValidationException(String message, Throwable cause, List<ObjectError> objectErrors) {
        super(message, cause);
        this.objectErrors = objectErrors;
    }
}
