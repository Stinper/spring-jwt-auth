package me.stinper.jwtauth.exception;

/**
 * Исключение, выбрасываемое в том случае, если валидатор не поддерживает переданный ему тип
 * @see org.springframework.validation.Validator
 */
public class ValidatorUnsupportedTypeException extends RuntimeException {
    public ValidatorUnsupportedTypeException() {}

    public ValidatorUnsupportedTypeException(String message) {
        super(message);
    }

    public ValidatorUnsupportedTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
