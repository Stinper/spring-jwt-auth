package me.stinper.commons.api.response.beanvalidation.exception;

import me.stinper.commons.api.response.beanvalidation.provider.registry.ConstraintAnnotationErrorResponseProvidersRegistry;

/**
 * Выбрасывается в случае, если была передана аннотация, для которой не был предоставлен соответствующий провайдер
 * @see ConstraintAnnotationErrorResponseProvidersRegistry
 */
public class NoSuchErrorResponseProvider extends RuntimeException {
    public NoSuchErrorResponseProvider() {}

    public NoSuchErrorResponseProvider(String message) {
        super(message);
    }

    public NoSuchErrorResponseProvider(String message, Throwable cause) {
        super(message, cause);
    }
}
