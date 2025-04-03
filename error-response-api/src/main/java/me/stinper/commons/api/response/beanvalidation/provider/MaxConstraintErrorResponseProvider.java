package me.stinper.commons.api.response.beanvalidation.provider;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Max;
import me.stinper.commons.api.response.beanvalidation.ConstraintViolationProblemDetails;
import me.stinper.commons.api.response.beanvalidation.MinMaxConstraintViolationDetails;
import me.stinper.commons.api.response.beanvalidation.path.PropertyPathExtractor;

import java.lang.annotation.Annotation;

public class MaxConstraintErrorResponseProvider implements ConstraintAnnotationErrorResponseProvider {
    @Override
    public boolean supports(Class<? extends Annotation> annotationClass) {
        return Max.class.isAssignableFrom(annotationClass);
    }

    @Override
    public ConstraintViolationProblemDetails buildErrorResponseDetails(ConstraintViolation<?> constraintViolation,
                                                                       PropertyPathExtractor propertyPathExtractor) {
        Annotation constraintAnnotation = this.extractIfSupportsOrThrow(constraintViolation);
        Max max = (Max) constraintAnnotation;

        return new ConstraintViolationProblemDetails(
                propertyPathExtractor.extractFrom(constraintViolation),
                constraintViolation.getMessage(),
                new MinMaxConstraintViolationDetails<Void, Long>(
                        null,
                        max.value()
                )
        );
    }
}
