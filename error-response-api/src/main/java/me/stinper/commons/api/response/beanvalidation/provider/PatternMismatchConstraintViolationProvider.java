package me.stinper.commons.api.response.beanvalidation.provider;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Pattern;
import me.stinper.commons.api.response.beanvalidation.ConstraintViolationProblemDetails;
import me.stinper.commons.api.response.beanvalidation.PatternMismatchConstraintViolationDetails;
import me.stinper.commons.api.response.beanvalidation.path.PropertyPathExtractor;

import java.lang.annotation.Annotation;

public class PatternMismatchConstraintViolationProvider implements ConstraintAnnotationErrorResponseProvider {
    @Override
    public boolean supports(Class<? extends Annotation> annotationClass) {
        return Pattern.class.isAssignableFrom(annotationClass);
    }

    @Override
    public ConstraintViolationProblemDetails buildErrorResponseDetails(ConstraintViolation<?> constraintViolation,
                                                                       PropertyPathExtractor propertyPathExtractor) {
        Pattern pattern = extractAsIfSupportsOrThrow(constraintViolation, Pattern.class);

        return new ConstraintViolationProblemDetails(
                propertyPathExtractor.extractFrom(constraintViolation),
                constraintViolation.getMessage(),
                new PatternMismatchConstraintViolationDetails(pattern.regexp())
        );
    }
}
