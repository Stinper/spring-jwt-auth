package me.stinper.commons.api.response.beanvalidation.exception;

/**
 * Выбрасывается в случае, если провайдер не поддерживает переданный тип аннотации
 * @see me.stinper.commons.api.response.beanvalidation.provider.ConstraintAnnotationErrorResponseProvider
 */
public class UnsupportedConstraintAnnotationType extends RuntimeException {
    public UnsupportedConstraintAnnotationType() {}

    public UnsupportedConstraintAnnotationType(String message) {
        super(message);
    }

    public UnsupportedConstraintAnnotationType(String message, Throwable cause) {
        super(message, cause);
    }
}
