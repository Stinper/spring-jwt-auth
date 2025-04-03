package me.stinper.commons.api.response.beanvalidation.provider;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Email;
import me.stinper.commons.api.response.beanvalidation.ConstraintViolationProblemDetails;
import me.stinper.commons.api.response.beanvalidation.PatternMismatchConstraintViolationDetails;
import me.stinper.commons.api.response.beanvalidation.path.PropertyPathExtractor;

import java.lang.annotation.Annotation;

public class EmailConstraintErrorResponseProvider implements ConstraintAnnotationErrorResponseProvider {

    @Override
    public boolean supports(Class<? extends Annotation> annotationClass) {
        return Email.class.isAssignableFrom(annotationClass);
    }

    @Override
    public ConstraintViolationProblemDetails buildErrorResponseDetails(ConstraintViolation<?> constraintViolation,
                                                                       PropertyPathExtractor propertyPathExtractor) {
        Email email = this.extractAsIfSupportsOrThrow(constraintViolation, Email.class);

        return new ConstraintViolationProblemDetails(
                propertyPathExtractor.extractFrom(constraintViolation),
                constraintViolation.getMessage(),
                new PatternMismatchConstraintViolationDetails(
                        email.regexp()
                )
        );
    }
}
