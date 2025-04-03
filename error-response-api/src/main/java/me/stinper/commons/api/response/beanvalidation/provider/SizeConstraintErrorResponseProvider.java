package me.stinper.commons.api.response.beanvalidation.provider;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Size;
import me.stinper.commons.api.response.beanvalidation.ConstraintViolationProblemDetails;
import me.stinper.commons.api.response.beanvalidation.MinMaxConstraintViolationDetails;
import me.stinper.commons.api.response.beanvalidation.path.PropertyPathExtractor;

import java.lang.annotation.Annotation;

public class SizeConstraintErrorResponseProvider implements ConstraintAnnotationErrorResponseProvider {
    @Override
    public boolean supports(Class<? extends Annotation> annotationClass) {
        return Size.class.isAssignableFrom(annotationClass);
    }

    @Override
    public ConstraintViolationProblemDetails buildErrorResponseDetails(ConstraintViolation<?> constraintViolation,
                                                                       PropertyPathExtractor propertyPathExtractor) {
        Size size = this.extractAsIfSupportsOrThrow(constraintViolation, Size.class);

        return new ConstraintViolationProblemDetails(
                propertyPathExtractor.extractFrom(constraintViolation),
                constraintViolation.getMessage(),
                new MinMaxConstraintViolationDetails<>(
                        size.min(),
                        size.max()
                )
        );
    }
}
