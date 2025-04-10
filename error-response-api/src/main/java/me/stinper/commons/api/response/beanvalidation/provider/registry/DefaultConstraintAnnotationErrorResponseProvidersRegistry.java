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
            Map.ofEntries(
                    Map.entry(NotNull.class,        new DefaultErrorResponseProvider(ConstraintViolationType.NOT_NULL)),
                    Map.entry(NotBlank.class,       new DefaultErrorResponseProvider(ConstraintViolationType.NOT_BLANK)),
                    Map.entry(Size.class,           new SizeConstraintErrorResponseProvider()),
                    Map.entry(Email.class,          new EmailConstraintErrorResponseProvider()),
                    Map.entry(Min.class,            new MinConstraintErrorResponseProvider()),
                    Map.entry(Max.class,            new MaxConstraintErrorResponseProvider()),
                    Map.entry(Positive.class,       new DefaultErrorResponseProvider(ConstraintViolationType.POSITIVE)),
                    Map.entry(PositiveOrZero.class, new DefaultErrorResponseProvider(ConstraintViolationType.POSITIVE_OR_ZERO)),
                    Map.entry(Negative.class,       new DefaultErrorResponseProvider(ConstraintViolationType.NEGATIVE)),
                    Map.entry(NegativeOrZero.class, new DefaultErrorResponseProvider(ConstraintViolationType.NEGATIVE_OR_ZERO)),
                    Map.entry(Pattern.class,        new PatternMismatchConstraintViolationProvider())
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
