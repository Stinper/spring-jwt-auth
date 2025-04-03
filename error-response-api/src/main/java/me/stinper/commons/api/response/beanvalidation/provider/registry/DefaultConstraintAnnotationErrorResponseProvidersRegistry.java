package me.stinper.commons.api.response.beanvalidation.provider.registry;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.*;
import me.stinper.commons.api.response.beanvalidation.enums.ConstraintViolationType;
import me.stinper.commons.api.response.beanvalidation.exception.NoSuchErrorResponseProvider;
import me.stinper.commons.api.response.beanvalidation.provider.*;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public final class DefaultConstraintAnnotationErrorResponseProvidersRegistry implements ConstraintAnnotationErrorResponseProvidersRegistry {
    private final Map<Class<? extends Annotation>, ConstraintAnnotationErrorResponseProvider> providers = new HashMap<>(
            Map.of(
                    NotNull.class,          new DefaultErrorResponseProvider(ConstraintViolationType.NOT_NULL),
                    NotBlank.class,         new DefaultErrorResponseProvider(ConstraintViolationType.NOT_BLANK),
                    Size.class,             new SizeConstraintErrorResponseProvider(),
                    Email.class,            new EmailConstraintErrorResponseProvider(),
                    Min.class,              new MinConstraintErrorResponseProvider(),
                    Max.class,              new MaxConstraintErrorResponseProvider(),
                    Positive.class,         new DefaultErrorResponseProvider(ConstraintViolationType.POSITIVE),
                    PositiveOrZero.class,   new DefaultErrorResponseProvider(ConstraintViolationType.POSITIVE_OR_ZERO),
                    Negative.class,         new DefaultErrorResponseProvider(ConstraintViolationType.NEGATIVE),
                    NegativeOrZero.class,   new DefaultErrorResponseProvider(ConstraintViolationType.NEGATIVE_OR_ZERO)
            )
    );

    @Override
    public void registerProvider(Class<? extends Annotation> constraintAnnotationClass, ConstraintAnnotationErrorResponseProvider provider) {
        providers.put(constraintAnnotationClass, provider);
    }

    @Override
    public ConstraintAnnotationErrorResponseProvider getResponseProviderFor(ConstraintViolation<?> constraintViolation) {
        Annotation constraintAnnotation = constraintViolation.getConstraintDescriptor().getAnnotation();

        if (!providers.containsKey(constraintAnnotation.annotationType())) {
            if (this.onUnsupportedAnnotationType() == UnsupportedAnnotationPassedAction.CALL_DEFAULT_PROVIDER)
                return this.getDefaultProvider();

            throw new NoSuchErrorResponseProvider(
                    "No provider found for constraint annotation type " + constraintAnnotation.annotationType().getName()
            );
        }

        return providers.get(constraintAnnotation.annotationType());

    }

}
