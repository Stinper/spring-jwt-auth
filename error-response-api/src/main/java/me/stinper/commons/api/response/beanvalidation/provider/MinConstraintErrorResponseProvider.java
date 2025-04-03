package me.stinper.commons.api.response.beanvalidation.provider;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Min;
import me.stinper.commons.api.response.beanvalidation.ConstraintViolationProblemDetails;
import me.stinper.commons.api.response.beanvalidation.MinMaxConstraintViolationDetails;
import me.stinper.commons.api.response.beanvalidation.path.PropertyPathExtractor;

import java.lang.annotation.Annotation;

public class MinConstraintErrorResponseProvider implements ConstraintAnnotationErrorResponseProvider {
    @Override
    public boolean supports(Class<? extends Annotation> annotationClass) {
        return Min.class.isAssignableFrom(annotationClass);
    }

    @Override
    public ConstraintViolationProblemDetails buildErrorResponseDetails(ConstraintViolation<?> constraintViolation,
                                                                       PropertyPathExtractor propertyPathExtractor) {
        Annotation constraintAnnotation = this.extractIfSupportsOrThrow(constraintViolation);
        Min min = (Min) constraintAnnotation;

        return new ConstraintViolationProblemDetails(
                propertyPathExtractor.extractFrom(constraintViolation),
                constraintViolation.getMessage(),
                new MinMaxConstraintViolationDetails<Long, Void>(
                        min.value(),
                        null
                )
        );
    }
}
